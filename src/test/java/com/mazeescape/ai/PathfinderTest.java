package com.mazeescape.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Position;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

import java.util.List;

class PathfinderTest {

    private final Pathfinder pathfinder = new Pathfinder();

    // '#' = wall, '.' = floor
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
    void shortestPathInOpenGrid() {
        Maze maze = build("...", "...", "...");
        List<Position> path = pathfinder.findPath(maze, new Position(0, 0), new Position(2, 2));
        assertEquals(5, path.size());
        assertEquals(new Position(0, 0), path.get(0));
        assertEquals(new Position(2, 2), path.get(path.size() - 1));
    }

    @Test
    void goesAroundAWall() {
        Maze maze = build(".#.", ".#.", "...");
        List<Position> path = pathfinder.findPath(maze, new Position(0, 0), new Position(2, 0));
        assertEquals(7, path.size());
    }

    @Test
    void returnsEmptyWhenNoPathExists() {
        Maze maze = build(".#.");
        assertTrue(pathfinder.findPath(maze, new Position(0, 0), new Position(2, 0)).isEmpty());
    }

    @Test
    void startEqualsGoal() {
        Maze maze = build("...");
        List<Position> path = pathfinder.findPath(maze, new Position(1, 0), new Position(1, 0));
        assertEquals(List.of(new Position(1, 0)), path);
    }
}