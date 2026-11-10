// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Bomber;
import com.mazeescape.entities.enemies.Chaser;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.Item;
import com.mazeescape.entities.items.Key;
import com.mazeescape.entities.items.Shield;
import com.mazeescape.entities.items.SpeedBoost;
import com.mazeescape.entities.items.Taser;
import com.mazeescape.entities.items.TimeStop;
import com.mazeescape.save.SaveData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Builds the levels and moves between them: new game, restart after a death,
 * next level, and loading a save.
 * <p>
 * The maze, the enemies and the items only depend on the seed, so a restart gives
 * the same level. The key is placed with another random generator, so it moves
 * on every restart.
 */
public class LevelManager {

    /** Number of levels in the game. */
    public static final int MAX_LEVEL = 3;
    /** Base speed of the player, in tiles per second. */
    public static final double PLAYER_SPEED = 3.0;
    /** Radius of every enemy's detection zone, in tiles. */
    public static final double DETECTION_RADIUS = 6.0;
    /** Speed of every Chaser, in tiles per second: slower than the player. */
    public static final double CHASER_SPEED = 2.0;

    /** Enemies never appear closer than this (in steps) to the start. */
    private static final int ENEMY_MIN_DISTANCE = 8;
    /** Items never appear closer than this (in steps) to the start. */
    private static final int ITEM_MIN_DISTANCE = 3;

    private final Random random;
    private int level;
    private long seed;
    private Position lastKey;

    /**
     * Creates a manager. Tests pass a seeded generator so results never change.
     *
     * @param random decides new seeds and key positions
     */
    public LevelManager(Random random) {
        this.random = random;
    }

    /** Creates a manager with a truly random generator, used by the real game. */
    public LevelManager() {
        this(new Random());
    }

    /**
     * Size of the (square) maze of a level: 13, then 15, then 17 tiles.
     *
     * @param level level number, from 1
     * @return an odd size
     */
    public static int sizeFor(int level) {
        return 11 + 2 * level;
    }

    /**
     * Starts a new game at level 1 on a new random maze.
     *
     * @return the game, ready to play
     */
    public Game newGame() {
        return load(new SaveData(1, random.nextLong()));
    }

    /**
     * Starts from a saved game.
     *
     * @param data level number and maze seed
     * @return the game, ready to play
     * @throws IllegalArgumentException if the level is outside 1 to {@link #MAX_LEVEL}
     */
    public Game load(SaveData data) {
        if (data.level() < 1 || data.level() > MAX_LEVEL) {
            throw new IllegalArgumentException("No level " + data.level());
        }
        level = data.level();
        seed = data.seed();
        return build(null);
    }

    /**
     * Restarts the current level after a death: same maze, same enemies and items,
     * the key somewhere else.
     *
     * @return a fresh game on the same level
     */
    public Game restart() {
        requireStarted();
        return build(lastKey);
    }

    /**
     * Tells whether a level comes after the current one.
     *
     * @return true below level {@link #MAX_LEVEL}
     */
    public boolean hasNextLevel() {
        requireStarted();
        return level < MAX_LEVEL;
    }

    /**
     * Moves to the next level, on a new maze.
     *
     * @return the game on the new level
     * @throws IllegalStateException if the current level is the last one
     */
    public Game nextLevel() {
        if (!hasNextLevel()) {
            throw new IllegalStateException("Level " + MAX_LEVEL + " is the last one");
        }
        level++;
        seed = random.nextLong();
        return build(null);
    }

    /**
     * What must go in the save file to come back to this level.
     *
     * @return level number and seed
     */
    public SaveData toSaveData() {
        requireStarted();
        return new SaveData(level, seed);
    }

    /**
     * Returns the current level number.
     *
     * @return from 1 to {@link #MAX_LEVEL}, or 0 before the first game
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the key position chosen for the current level.
     *
     * @return the key position, or null before the first game
     */
    public Position getKeyPosition() {
        return lastKey;
    }

    private Game build(Position keyToAvoid) {
        int size = sizeFor(level);
        Maze maze = new MazeGenerator().generate(size, size, seed);
        Game game = new Game();
        game.setLevel(level);
        game.setMaze(maze);
        game.setPlayer(new Player(maze.getStart(), PLAYER_SPEED));

        int[][] distance = maze.distancesFrom(maze.getStart());
        Set<Position> taken = new HashSet<>();
        taken.add(maze.getStart());
        Random layout = new Random(seed * 31 + 7);

        // No enemy on the shortest route from the start to the exit: the level is always winnable.
        Set<Position> forEnemies = new HashSet<>(taken);
        forEnemies.addAll(routeToExit(maze));
        List<Position> enemySpots = freeCells(maze, distance, ENEMY_MIN_DISTANCE, forEnemies, layout);
        if (enemySpots.size() < 5) {
            enemySpots = freeCells(maze, distance, ENEMY_MIN_DISTANCE, taken, layout);
        }
        Set<Position> enemyCells = new HashSet<>();
        for (Enemy enemy : enemiesFor(level, enemySpots)) {
            game.getEnemies().add(enemy);
            taken.add(enemy.getPosition());
            enemyCells.add(enemy.getPosition());
        }
        int[][] safe = distancesAvoiding(maze, maze.getStart(), enemyCells);
        List<Position> itemCells = freeCells(maze, distance, ITEM_MIN_DISTANCE, taken, layout);
        Item[] items = {
            new SpeedBoost(itemCells.get(0)), new Taser(itemCells.get(1)),
            new Shield(itemCells.get(2)), new TimeStop(itemCells.get(3))
        };
        for (Item item : items) {
            game.getItems().add(item);
            taken.add(item.getPosition());
        }
        lastKey = placeKey(maze, distance, safe, taken, keyToAvoid);
        game.getItems().add(new Key(lastKey));
        return game;
    }

    /** Level 1: one of each enemy. Level 2 adds a Bomber, level 3 adds a Chaser. */
    private List<Enemy> enemiesFor(int level, List<Position> cells) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Chaser(cells.get(0), DETECTION_RADIUS, CHASER_SPEED));
        enemies.add(new Bomber(cells.get(1), DETECTION_RADIUS));
        enemies.add(new Leech(cells.get(2), DETECTION_RADIUS));
        if (level >= 2) {
            enemies.add(new Bomber(cells.get(3), DETECTION_RADIUS));
        }
        if (level >= 3) {
            enemies.add(new Chaser(cells.get(4), DETECTION_RADIUS, CHASER_SPEED));
        }
        return enemies;
    }

    /** Free floor cells at least {@code minDistance} steps away, in a shuffled order. */
    private List<Position> freeCells(Maze maze, int[][] distance, int minDistance,
                                     Set<Position> taken, Random random) {
        List<Position> cells = new ArrayList<>();
        for (Position p : maze.floorPositions()) {
            if (distance[p.y()][p.x()] >= minDistance && !taken.contains(p)) {
                cells.add(p);
            }
        }
        Collections.shuffle(cells, random);
        return cells;
    }

    /**
     * The key goes on a free cell in the farthest half of the maze (found by BFS) that the
     * player can reach without walking on an enemy. If no far cell is safe, any safe cell is used.
     */
    private Position placeKey(Maze maze, int[][] distance, int[][] safe, Set<Position> taken,
                              Position avoid) {
        int max = 0;
        for (int[] row : distance) {
            for (int d : row) {
                max = Math.max(max, d);
            }
        }
        List<Position> candidates = new ArrayList<>();
        List<Position> anySafe = new ArrayList<>();
        for (Position p : maze.floorPositions()) {
            if (taken.contains(p) || safe[p.y()][p.x()] < 0) {
                continue;
            }
            anySafe.add(p);
            if (distance[p.y()][p.x()] >= max / 2) {
                candidates.add(p);
            }
        }
        if (candidates.isEmpty()) {
            candidates = anySafe;
        }
        if (candidates.size() > 1 && avoid != null) {
            candidates.remove(avoid);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * The cells of the shortest route from the start to the exit, through the door.
     *
     * @param maze the maze
     * @return every cell of that route, start and exit included
     */
    static Set<Position> routeToExit(Maze maze) {
        int[][] toExit = distancesAvoiding(maze, maze.getExit(), Set.of());
        Set<Position> route = new HashSet<>();
        Position current = maze.getStart();
        route.add(current);
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (toExit[current.y()][current.x()] > 0) {
            for (int[] m : moves) {
                Position next = new Position(current.x() + m[0], current.y() + m[1]);
                if (maze.inBounds(next)
                        && toExit[next.y()][next.x()] == toExit[current.y()][current.x()] - 1) {
                    current = next;
                    break;
                }
            }
            route.add(current);
        }
        return route;
    }

    /**
     * Breadth-first search from {@code from} that never steps on a banned cell. The door counts
     * as open, because the player will hold the key when he gets there.
     *
     * @param maze   the maze
     * @param from   where the search starts
     * @param banned cells that must not be walked on (the enemies)
     * @return {@code distance[y][x]}, or -1 where the cell cannot be reached safely
     */
    static int[][] distancesAvoiding(Maze maze, Position from, Set<Position> banned) {
        int[][] distance = new int[maze.getHeight()][maze.getWidth()];
        for (int[] row : distance) {
            Arrays.fill(row, -1);
        }
        Deque<Position> queue = new ArrayDeque<>();
        distance[from.y()][from.x()] = 0;
        queue.add(from);
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            for (int[] m : moves) {
                Position next = new Position(current.x() + m[0], current.y() + m[1]);
                if (!maze.inBounds(next) || distance[next.y()][next.x()] != -1
                        || banned.contains(next)) {
                    continue;
                }
                Tile tile = maze.getTile(next);
                if (tile.isBlocking() && !(tile instanceof Door)) {
                    continue;
                }
                distance[next.y()][next.x()] = distance[current.y()][current.x()] + 1;
                queue.add(next);
            }
        }
        return distance;
    }

    private void requireStarted() {
        if (level == 0) {
            throw new IllegalStateException("No level yet: call newGame() or load() first");
        }
    }
}
