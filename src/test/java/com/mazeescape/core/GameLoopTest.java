// Owner: Totti
package com.mazeescape.core;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Chaser;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

class GameLoopTest {

    private Maze buildMaze(String... rows) {
        Maze maze = new Maze(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                maze.setTile(new Position(x, y),
                        rows[y].charAt(x) == '#' ? new Wall() : new Floor());
            }
        }
        return maze;
    }

    @Test
    void playerCannotMoveIntoWall() {
        Maze maze = buildMaze(
            "###",
            "#.#",
            "###"
        );
        Player player = new Player(new Position(1, 1), 4);
        
        // Try to move left into a wall
        boolean moved = player.move(Direction.LEFT, maze);
        
        assertFalse(moved);
        assertEquals(new Position(1, 1), player.getPosition());
    }

    @Test
    void playerCanMoveIntoFloor() {
        Maze maze = buildMaze(
            "###",
            "#..",
            "###"
        );
        Player player = new Player(new Position(1, 1), 4);
        
        boolean moved = player.move(Direction.RIGHT, maze);
        
        assertTrue(moved);
        assertEquals(new Position(2, 1), player.getPosition());
    }

    @Test
    void steppingOnChaserKillsPlayer() {
        Maze maze = buildMaze(
            "###",
            "#..",
            "###"
        );
        Player player = new Player(new Position(1, 1), 4);
        Chaser chaser = new Chaser(new Position(2, 1), 10);
        
        Game game = new Game();
        game.setMaze(maze);
        game.setPlayer(player);
        game.getEnemies().add(chaser);
        
        // Move player onto the Chaser
        player.move(Direction.RIGHT, maze);
        game.update(1.0); // Trigger the loop
        
        assertFalse(player.isAlive());
    }
}