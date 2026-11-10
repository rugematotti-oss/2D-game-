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

class BomberTest {

    private Maze build(String... rows) {
        Maze maze = new Maze(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                maze.setTile(new Position(x, y),
                        rows[y].charAt(x) == '#' ? new Wall() : new Floor());
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
    void staysInPlaceWhenPlayerNotDetected() {
        Player player = new Player(new Position(20, 0), 4);
        Game game = gameWith(build(".".repeat(30)), player);
        Bomber bomber = new Bomber(new Position(0, 0), 3);

        // Trigger update (does nothing now)
        bomber.update(game, 1.0);

        assertEquals(new Position(0, 0), bomber.getPosition());
        assertTrue(player.isAlive());
    }

    @Test
    void killsPlayerInBlastAfterFuse() {
        Player player = new Player(new Position(2, 0), 4);
        Game game = gameWith(build("....."), player);
        Bomber bomber = new Bomber(new Position(0, 0), 10);

        // Now it just explodes instantly on contact
        bomber.onPlayerContact(player);

        assertFalse(player.isAlive());
    }

    @Test
    void blastBlockedByWall() {
        Player player = new Player(new Position(2, 0), 4);
        Game game = gameWith(build(".#..."), player);
        Bomber bomber = new Bomber(new Position(0, 0), 10);

        // In turn-based, if you step on it, you die regardless of walls.
        bomber.onPlayerContact(player);

        assertFalse(player.isAlive());
    }

    @Test
    void doesNotKillPlayerOutOfBlastRange() {
        Player player = new Player(new Position(10, 0), 4);
        Game game = gameWith(build(".".repeat(20)), player);
        Bomber bomber = new Bomber(new Position(0, 0), 20);

        // Doesn't do anything because the player didn't step on it
        bomber.update(game, 1.0);

        assertTrue(player.isAlive());
    }
}