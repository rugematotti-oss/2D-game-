// Owner: Rachid
package com.mazeescape.maze;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Position;
import org.junit.jupiter.api.Test;

class MazeTest {

    private Maze build(String... rows) {
        Maze maze = new Maze(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                maze.setTile(new Position(x, y), rows[y].charAt(x) == '#' ? new Wall() : new Floor());
            }
        }
        return maze;
    }

    @Test
    void distancesCountStepsAndSkipWalls() {
        Maze maze = build(
            "#####",
            "#...#",
            "###.#",
            "#...#",
            "#####");
        int[][] d = maze.distancesFrom(new Position(1, 1));
        assertEquals(0, d[1][1]);
        assertEquals(2, d[1][3]);
        assertEquals(6, d[3][1]);
        assertEquals(-1, d[0][0]);
    }

    @Test
    void boundsAreChecked() {
        Maze maze = build("...", "...");
        assertTrue(maze.inBounds(new Position(2, 1)));
        assertFalse(maze.inBounds(new Position(3, 0)));
        assertFalse(maze.inBounds(new Position(0, -1)));
    }

    @Test
    void neighboursIgnoreWallsAndBorders() {
        Maze maze = build(
            "#.#",
            "...",
            "###");
        assertEquals(3, maze.openNeighbours(new Position(1, 1)).size());
        assertEquals(1, maze.openNeighbours(new Position(0, 1)).size());
    }

    @Test
    void asciiAndFloorList() {
        Maze maze = build("#.", ".#");
        assertEquals("#.\n.#\n", maze.toAscii());
        assertEquals(2, maze.floorPositions().size());
    }

    @Test
    void handMadeMazeStartsAtOneOne() {
        Maze maze = build("...", "...");
        assertEquals(new Position(1, 1), maze.getStart());
        assertNull(maze.getDoor());
        assertNull(maze.getExit());
    }
}
