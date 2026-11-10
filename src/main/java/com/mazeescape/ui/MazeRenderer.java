// Owner: Totti
package com.mazeescape.ui;

import com.mazeescape.core.Position;
import com.mazeescape.maze.Maze;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Draws the maze tiles using JavaFX GraphicsContext on the Canvas. */
public class MazeRenderer {

    public void render(GraphicsContext gc, Maze maze, int tileSize, double offsetX, double offsetY) {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                double px = offsetX + (x * tileSize);
                double py = offsetY + (y * tileSize);
                
                char symbol = maze.getTile(new Position(x, y)).symbol();

                switch (symbol) {
                    case '#' -> drawWall(gc, maze, x, y, px, py, tileSize);
                    case 'D' -> drawDoor(gc, px, py, tileSize);
                    case 'E' -> drawExit(gc, px, py, tileSize);
                    default  -> drawFloor(gc, px, py, tileSize);
                }
            }
        }
    }

    private void drawFloor(GraphicsContext gc, double x, double y, int size) {
        gc.setFill(Color.web("#2c3e50")); // Smooth dark blue/gray floor
        gc.fillRect(x, y, size, size);
        
        gc.setStroke(Color.web("#233140")); // Very subtle grid line
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
    }

    private void drawWall(GraphicsContext gc, Maze maze, int gridX, int gridY, double x, double y, int size) {
        // 1. Draw the solid base of the wall
        gc.setFill(Color.web("#34495e")); // Slate gray
        gc.fillRect(x, y, size, size);

        // 2. Draw smooth edges only if there is NO wall next to it
        gc.setLineWidth(4);

        // Top Edge (Highlight)
        if (!isWall(maze, gridX, gridY - 1)) {
            gc.setStroke(Color.web("#4a6a8a")); // Lighter blue-gray
            gc.strokeLine(x, y + 2, x + size, y + 2);
        }

        // Left Edge (Highlight)
        if (!isWall(maze, gridX - 1, gridY)) {
            gc.setStroke(Color.web("#4a6a8a"));
            gc.strokeLine(x + 2, y, x + 2, y + size);
        }

        // Bottom Edge (Shadow)
        if (!isWall(maze, gridX, gridY + 1)) {
            gc.setStroke(Color.web("#1a252f")); // Darker shadow
            gc.strokeLine(x, y + size - 2, x + size, y + size - 2);
        }

        // Right Edge (Shadow)
        if (!isWall(maze, gridX + 1, gridY)) {
            gc.setStroke(Color.web("#1a252f"));
            gc.strokeLine(x + size - 2, y, x + size - 2, y + size);
        }
    }

    /** Helper to check if a specific grid cell is a wall. */
    private boolean isWall(Maze maze, int x, int y) {
        // Treat out-of-bounds as walls so the map border looks clean
        if (x < 0 || y < 0 || x >= maze.getWidth() || y >= maze.getHeight()) {
            return true; 
        }
        return maze.getTile(new Position(x, y)).symbol() == '#';
    }

    private void drawDoor(GraphicsContext gc, double x, double y, int size) {
        // Smooth wooden door
        gc.setFill(Color.web("#3e2723")); // Dark brown frame
        gc.fillRect(x, y, size, size);

        gc.setFill(Color.web("#8d6e63")); // Light wood
        gc.fillRect(x + 4, y + 4, size - 8, size - 8);

        // Door planks (vertical lines)
        gc.setStroke(Color.web("#5d4037"));
        gc.setLineWidth(2);
        gc.strokeLine(x + size/3.0, y + 4, x + size/3.0, y + size - 4);
        gc.strokeLine(x + (2*size)/3.0, y + 4, x + (2*size)/3.0, y + size - 4);

        // Golden door knob
        gc.setFill(Color.GOLD);
        gc.fillOval(x + size - 14, y + size / 2.0 - 4, 8, 8);
    }

    private void drawExit(GraphicsContext gc, double x, double y, int size) {
        gc.setFill(Color.web("#1b5e20")); // Dark green base
        gc.fillRect(x, y, size, size);

        // Glowing portal effect
        gc.setFill(Color.web("#00e676")); // Bright green
        gc.fillOval(x + 6, y + 6, size - 12, size - 12);
        
        gc.setFill(Color.web("#69f0ae")); // Lighter green center
        gc.fillOval(x + 12, y + 12, size - 24, size - 24);
    }
}