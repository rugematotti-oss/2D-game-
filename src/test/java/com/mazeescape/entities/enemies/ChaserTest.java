// Owner: Totti
package com.mazeescape.entities.enemies;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

class ChaserTest {

    private Maze build(String... rows) {
        Maze maze = new Maze(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                maze.setTile(new Position(x, y), rows[y].charAt(x) == '#' ? new Wall() : new Floor());
            }
        }
        return maze;
    }

    private Game gameWith(Maze maze, Player player) {
        Game game = new Game();
        game.setMaze(maze);
        game.setPlayer(player);
        return game;
    }

    @Test
    void detectsPlayerInRange() {
        Player player = new Player(new Position(2, 0), 4);
        Game game = gameWith(build("........"), player);
        Chaser chaser = new Chaser(new Position(0, 0), 3.0);

        assertTrue(chaser.detects(player, game.getMaze()), "Chaser should detect the player in range");
    }

    @Test
    void doesNotDetectPlayerOutOfRange() {
        Player player = new Player(new Position(5, 0), 4);
        Game game = gameWith(build(".........."), player);
        Chaser chaser = new Chaser(new Position(0, 0), 3.0);

        assertFalse(chaser.detects(player, game.getMaze()), "Chaser should not detect the player out of range");
    }

    @Test
    void killsThePlayerOnContact() {
        Player player = new Player(new Position(1, 0), 4);
        Game game = gameWith(build("........"), player);
        Chaser chaser = new Chaser(new Position(0, 0), 3.0);

        chaser.onPlayerContact(player);

        assertFalse(player.isAlive(), "Player should die when stepping on the Chaser");
    }

    @Test
    void chaserNeverMovesThroughWalls() {
        // A maze with a solid wall in the middle, but a gap at the bottom
        // # # # # #
        // # C . . #
        // # # # . #
        // # . . P #
        // # # # # #
        Maze maze = build(
            "#####",
            "#C..#",
            "###.#",
            "#..P#",
            "#####"
        );

        Player player = new Player(new Position(3, 3), 4);
        Game game = gameWith(maze, player);
        
        // Large radius so the Chaser is always active
        Chaser chaser = new Chaser(new Position(1, 1), 10.0, 2.0);

        // Simulate the Chaser moving for 5 seconds (50 frames at 0.1s each)
        for (int i = 0; i < 50; i++) {
            chaser.update(game, 0.1);
            
            Position pos = chaser.getPosition();
            
            // 1. Assert the Chaser is never on a wall
            assertFalse(maze.getTile(pos).isBlocking(), 
                "CRITICAL: Chaser moved into a wall at " + pos.x() + "," + pos.y());
                
            // 2. Assert the Chaser stays within the maze bounds
            assertTrue(maze.inBounds(pos), 
                "CRITICAL: Chaser moved out of bounds at " + pos.x() + "," + pos.y());
        }
    }
}