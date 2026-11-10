package com.mazeescape.ai;

import com.mazeescape.core.Position;
import com.mazeescape.maze.Maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Pathfinder {

    public List<Position> findPath(Maze maze, Position start, Position goal) {
        if (start.equals(goal)) {
            return List.of(start);
        }

        Queue<Position> queue = new ArrayDeque<>();
        Map<Position, Position> cameFrom = new HashMap<>();

        queue.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(goal)) {
                return rebuildPath(cameFrom, goal);
            }

            for (Position neighbor : neighborsOf(current, maze)) {
                if (!cameFrom.containsKey(neighbor)) {
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return List.of();
    }

    private List<Position> neighborsOf(Position p, Maze maze) {
        List<Position> result = new ArrayList<>();
        int[][] moves = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] m : moves) {
            int x = p.x() + m[0];
            int y = p.y() + m[1];
            if (x >= 0 && x < maze.getWidth() && y >= 0 && y < maze.getHeight()) {
                Position candidate = new Position(x, y);
                if (!maze.getTile(candidate).isBlocking()) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }

    private List<Position> rebuildPath(Map<Position, Position> cameFrom, Position goal) {
        LinkedList<Position> path = new LinkedList<>();
        Position current = goal;
        while (current != null) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        return path;
    }
}