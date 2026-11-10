// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.core.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * The grid of tiles for one level, plus its three special places:
 * the start, the door and the exit.
 */
public class Maze {

    /** The four moves without diagonals: right, left, down, up. */
    private static final int[][] MOVES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final Tile[][] grid;
    private Position start = new Position(1, 1);
    private Position door;
    private Position exit;

    /**
     * Creates an empty grid. Every cell must then be filled with {@link #setTile}.
     *
     * @param width  number of columns
     * @param height number of rows
     */
    public Maze(int width, int height) {
        this.grid = new Tile[height][width];
    }

    /**
     * Returns the number of columns.
     *
     * @return the width
     */
    public int getWidth() {
        return grid[0].length;
    }

    /**
     * Returns the number of rows.
     *
     * @return the height
     */
    public int getHeight() {
        return grid.length;
    }

    /**
     * Returns the tile at a position.
     *
     * @param p the position
     * @return the tile there
     */
    public Tile getTile(Position p) {
        return grid[p.y()][p.x()];
    }

    /**
     * Puts a tile at a position.
     *
     * @param p    the position
     * @param tile the new tile
     */
    public void setTile(Position p, Tile tile) {
        grid[p.y()][p.x()] = tile;
    }

    /**
     * Tells whether a position is inside the grid.
     *
     * @param p the position
     * @return true if it exists in this maze
     */
    public boolean inBounds(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < getWidth() && p.y() < getHeight();
    }

    /**
     * Returns where the player starts.
     *
     * @return the start position
     */
    public Position getStart() {
        return start;
    }

    /**
     * Returns where the door is.
     *
     * @return the door position, or null for a hand-made maze
     */
    public Position getDoor() {
        return door;
    }

    /**
     * Returns where the exit is.
     *
     * @return the exit position, or null for a hand-made maze
     */
    public Position getExit() {
        return exit;
    }

    void setSpecialPlaces(Position start, Position door, Position exit) {
        this.start = start;
        this.door = door;
        this.exit = exit;
    }

    /**
     * Lists the non-blocking neighbours of a position.
     *
     * @param p the position to look around
     * @return up to four positions
     */
    public List<Position> openNeighbours(Position p) {
        List<Position> result = new ArrayList<>();
        for (int[] m : MOVES) {
            Position next = new Position(p.x() + m[0], p.y() + m[1]);
            if (inBounds(next) && !getTile(next).isBlocking()) {
                result.add(next);
            }
        }
        return result;
    }

    /**
     * Counts the steps from one position to every other one, with a breadth-first
     * search: first every cell at 1 step, then at 2 steps, and so on.
     *
     * @param from the starting position
     * @return {@code distance[y][x]}, or -1 where the cell cannot be reached
     */
    public int[][] distancesFrom(Position from) {
        int[][] distance = new int[getHeight()][getWidth()];
        for (int[] row : distance) {
            Arrays.fill(row, -1);
        }
        Deque<Position> queue = new ArrayDeque<>();
        distance[from.y()][from.x()] = 0;
        queue.add(from);
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            for (Position next : openNeighbours(current)) {
                if (distance[next.y()][next.x()] == -1) {
                    distance[next.y()][next.x()] = distance[current.y()][current.x()] + 1;
                    queue.add(next);
                }
            }
        }
        return distance;
    }

    /**
     * Lists every plain floor cell. Used to place the key, the items and the enemies.
     *
     * @return all positions holding a {@link Floor}
     */
    public List<Position> floorPositions() {
        List<Position> result = new ArrayList<>();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (grid[y][x] instanceof Floor) {
                    result.add(new Position(x, y));
                }
            }
        }
        return result;
    }

    /**
     * Draws the maze as text, one character per tile.
     *
     * @return the maze, one line per row
     */
    public String toAscii() {
        StringBuilder sb = new StringBuilder();
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                sb.append(tile.symbol());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
