// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.core.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Seeded random maze generation, with extra loops so dead ends are not fatal.
 * <ol>
 *   <li>Fill the grid with walls.</li>
 *   <li>Dig corridors with a random walk that backs up when it is stuck
 *       (recursive backtracker).</li>
 *   <li>Put the exit in the dead end farthest from the start, and the door on the only
 *       square that leads to it.</li>
 *   <li>Knock down a few walls to create loops.</li>
 * </ol>
 * Rooms sit on odd coordinates; the squares between two rooms have one even coordinate.
 */
public class MazeGenerator {

    /** Share of the inner walls between two corridors that are knocked down. */
    private static final double LOOP_RATIO = 0.10;

    /** A step of two squares: from one room to the next. */
    private static final int[][] ROOM_STEPS = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};

    /** Creates a generator. */
    public MazeGenerator() {
    }

    /**
     * Builds a maze. The same seed always gives the same maze (used for restart,
     * save and tests).
     *
     * @param width  number of columns, odd and at least 7
     * @param height number of rows, odd and at least 7
     * @param seed   the number that decides the whole maze
     * @return the new maze
     * @throws IllegalArgumentException if a size is even or below 7
     */
    public Maze generate(int width, int height, long seed) {
        if (width < 7 || height < 7 || width % 2 == 0 || height % 2 == 0) {
            throw new IllegalArgumentException(
                    "Maze size must be odd and at least 7, got " + width + "x" + height);
        }
        Random random = new Random(seed);
        Maze maze = new Maze(width, height);
        fillWithWalls(maze);

        Position start = new Position(1, 1);
        carveCorridors(maze, start, random);

        Position exit = farthestDeadEnd(maze, start);
        Position door = maze.openNeighbours(exit).get(0);
        maze.setTile(exit, new Exit());
        maze.setTile(door, new Door());
        maze.setSpecialPlaces(start, door, exit);

        addLoops(maze, exit, random);
        return maze;
    }

    private void fillWithWalls(Maze maze) {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                maze.setTile(new Position(x, y), new Wall());
            }
        }
    }

    /** Digs forward at random; goes back when there is nowhere new to go. */
    private void carveCorridors(Maze maze, Position start, Random random) {
        Deque<Position> stack = new ArrayDeque<>();
        maze.setTile(start, new Floor());
        stack.push(start);
        while (!stack.isEmpty()) {
            Position current = stack.peek();
            List<int[]> steps = new ArrayList<>(List.of(ROOM_STEPS));
            Collections.shuffle(steps, random);
            boolean moved = false;
            for (int[] step : steps) {
                Position next = new Position(current.x() + step[0], current.y() + step[1]);
                if (isInner(maze, next) && maze.getTile(next) instanceof Wall) {
                    Position between = new Position(current.x() + step[0] / 2,
                            current.y() + step[1] / 2);
                    maze.setTile(between, new Floor());
                    maze.setTile(next, new Floor());
                    stack.push(next);
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                stack.pop();
            }
        }
    }

    /** The room with a single opening that is the longest walk from the start. */
    private Position farthestDeadEnd(Maze maze, Position start) {
        int[][] distance = maze.distancesFrom(start);
        Position best = null;
        for (int y = 1; y < maze.getHeight(); y += 2) {
            for (int x = 1; x < maze.getWidth(); x += 2) {
                Position p = new Position(x, y);
                boolean deadEnd = maze.openNeighbours(p).size() == 1;
                if (deadEnd && !p.equals(start)
                        && (best == null || distance[y][x] > distance[best.y()][best.x()])) {
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * Turns some walls into floor when they separate two corridors.
     * Walls touching the exit are kept, so the door stays its only entrance.
     */
    private void addLoops(Maze maze, Position exit, Random random) {
        for (int y = 1; y < maze.getHeight() - 1; y++) {
            for (int x = 1; x < maze.getWidth() - 1; x++) {
                Position p = new Position(x, y);
                if (!(maze.getTile(p) instanceof Wall)
                        || Math.abs(exit.x() - x) + Math.abs(exit.y() - y) == 1) {
                    continue;
                }
                boolean horizontal = isFloor(maze, x - 1, y) && isFloor(maze, x + 1, y);
                boolean vertical = isFloor(maze, x, y - 1) && isFloor(maze, x, y + 1);
                if ((horizontal || vertical) && random.nextDouble() < LOOP_RATIO) {
                    maze.setTile(p, new Floor());
                }
            }
        }
    }

    private boolean isFloor(Maze maze, int x, int y) {
        return maze.getTile(new Position(x, y)) instanceof Floor;
    }

    private boolean isInner(Maze maze, Position p) {
        return p.x() > 0 && p.y() > 0 && p.x() < maze.getWidth() - 1 && p.y() < maze.getHeight() - 1;
    }
}
