// Owner: Rachid
package com.mazeescape.maze;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Position;
import org.junit.jupiter.api.Test;

class MazeGeneratorTest {

    private final MazeGenerator generator = new MazeGenerator();

    @Test
    void sameSeedGivesSameMaze() {
        assertEquals(generator.generate(15, 15, 42).toAscii(),
                     generator.generate(15, 15, 42).toAscii());
    }

    @Test
    void differentSeedsGiveDifferentMazes() {
        assertNotEquals(generator.generate(15, 15, 1).toAscii(),
                        generator.generate(15, 15, 2).toAscii());
    }

    @Test
    void borderIsMadeOfWalls() {
        Maze maze = generator.generate(15, 11, 7);
        for (int x = 0; x < maze.getWidth(); x++) {
            assertInstanceOf(Wall.class, maze.getTile(new Position(x, 0)));
            assertInstanceOf(Wall.class, maze.getTile(new Position(x, maze.getHeight() - 1)));
        }
        for (int y = 0; y < maze.getHeight(); y++) {
            assertInstanceOf(Wall.class, maze.getTile(new Position(0, y)));
            assertInstanceOf(Wall.class, maze.getTile(new Position(maze.getWidth() - 1, y)));
        }
    }

    @Test
    void startDoorAndExitAreInPlace() {
        Maze maze = generator.generate(15, 15, 3);
        assertInstanceOf(Floor.class, maze.getTile(maze.getStart()));
        assertInstanceOf(Door.class, maze.getTile(maze.getDoor()));
        assertInstanceOf(Exit.class, maze.getTile(maze.getExit()));
    }

    @Test
    void everyFloorIsReachableFromTheStart() {
        for (long seed = 0; seed < 30; seed++) {
            Maze maze = generator.generate(17, 17, seed);
            int[][] distance = maze.distancesFrom(maze.getStart());
            for (Position p : maze.floorPositions()) {
                assertTrue(distance[p.y()][p.x()] >= 0, "seed " + seed + " at " + p);
            }
        }
    }

    @Test
    void exitCannotBeReachedWhileTheDoorIsClosed() {
        for (long seed = 0; seed < 30; seed++) {
            Maze maze = generator.generate(17, 17, seed);
            int[][] distance = maze.distancesFrom(maze.getStart());
            assertEquals(-1, distance[maze.getExit().y()][maze.getExit().x()], "seed " + seed);
        }
    }

    @Test
    void mazeHasLoops() {
        Maze maze = generator.generate(17, 17, 5);
        int floors = maze.floorPositions().size();
        int links = 0;
        for (Position p : maze.floorPositions()) {
            for (Position n : maze.openNeighbours(p)) {
                if (maze.getTile(n) instanceof Floor) {
                    links++;
                }
            }
        }
        // A maze with no loop is a tree: exactly (floors - 1) links. More means loops.
        assertTrue(links / 2 > floors - 1);
    }

    @Test
    void wrongSizesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(10, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(11, 10, 1));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(5, 11, 1));
    }
}
