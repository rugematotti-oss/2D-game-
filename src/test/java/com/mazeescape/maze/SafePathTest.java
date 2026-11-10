// Owner: Rachid
package com.mazeescape.maze;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.save.SaveData;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Every level must be winnable: key and exit reachable without walking on an enemy. */
class SafePathTest {

    private Set<Position> enemyCells(Game game) {
        Set<Position> cells = new HashSet<>();
        for (Enemy e : game.getEnemies()) {
            cells.add(e.getPosition());
        }
        return cells;
    }

    private void assertWinnable(Game game, Position key, String context) {
        Maze maze = game.getMaze();
        int[][] fromStart = LevelManager.distancesAvoiding(maze, maze.getStart(), enemyCells(game));
        assertTrue(fromStart[key.y()][key.x()] > 0, "key unreachable safely, " + context);
        int[][] fromKey = LevelManager.distancesAvoiding(maze, key, enemyCells(game));
        assertTrue(fromKey[maze.getExit().y()][maze.getExit().x()] > 0, "exit unreachable safely, " + context);
    }

    @Test
    void everyLevelHasASafePathToTheKeyAndTheExit() {
        for (int seed = 0; seed < 300; seed++) {
            for (int level = 1; level <= LevelManager.MAX_LEVEL; level++) {
                LevelManager lm = new LevelManager(new Random(seed));
                Game game = lm.load(new SaveData(level, seed * 7L + level));
                assertWinnable(game, lm.getKeyPosition(), "seed " + seed + " level " + level);
            }
        }
    }

    @Test
    void restartsKeepTheLevelWinnable() {
        for (int seed = 0; seed < 50; seed++) {
            LevelManager lm = new LevelManager(new Random(seed));
            lm.load(new SaveData(3, seed));
            for (int i = 0; i < 5; i++) {
                Game game = lm.restart();
                assertWinnable(game, lm.getKeyPosition(), "seed " + seed + " restart " + i);
            }
        }
    }

    @Test
    void searchDoesNotWalkOnBannedCells() {
        Maze maze = new Maze(5, 1);
        for (int x = 0; x < 5; x++) {
            maze.setTile(new Position(x, 0), new Floor());
        }
        int[][] d = LevelManager.distancesAvoiding(maze, new Position(0, 0), Set.of(new Position(2, 0)));
        assertEquals(1, d[0][1]);
        assertEquals(-1, d[0][3]);
    }

    @Test
    void searchGoesThroughAClosedDoor() {
        Maze maze = new Maze(3, 1);
        maze.setTile(new Position(0, 0), new Floor());
        maze.setTile(new Position(1, 0), new Door());
        maze.setTile(new Position(2, 0), new Exit());
        int[][] d = LevelManager.distancesAvoiding(maze, new Position(0, 0), Set.of());
        assertEquals(2, d[0][2]);
    }

    @Test
    void routeToExitLinksTheStartToTheExit() {
        Maze maze = new MazeGenerator().generate(13, 13, 5);
        Set<Position> route = LevelManager.routeToExit(maze);
        assertTrue(route.contains(maze.getStart()));
        assertTrue(route.contains(maze.getDoor()));
        assertTrue(route.contains(maze.getExit()));
    }

    @Test
    void noEnemyStandsOnTheRouteToTheExit() {
        for (int seed = 0; seed < 100; seed++) {
            LevelManager lm = new LevelManager(new Random(seed));
            Game game = lm.load(new SaveData(3, seed));
            Set<Position> route = LevelManager.routeToExit(game.getMaze());
            for (Enemy e : game.getEnemies()) {
                assertFalse(route.contains(e.getPosition()), "enemy on the route, seed " + seed);
            }
        }
    }
}
