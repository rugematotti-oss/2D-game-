// Owner: Rachid & Totti
package com.mazeescape.core;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Chaser;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.Key;
import com.mazeescape.entities.items.Shield;
import com.mazeescape.maze.Door;
import com.mazeescape.maze.Exit;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

/** Game.movePlayer: one player move = door, tile, item, then one turn of the world. */
class MovePlayerTest {

    /** '#' wall, '.' floor, 'D' door, 'E' exit. */
    private Game gameOn(String row) {
        Maze maze = new Maze(row.length(), 1);
        for (int x = 0; x < row.length(); x++) {
            char c = row.charAt(x);
            maze.setTile(new Position(x, 0),
                    c == '#' ? new Wall() : c == 'D' ? new Door() : c == 'E' ? new Exit() : new Floor());
        }
        Game game = new Game();
        game.setMaze(maze);
        game.setPlayer(new Player(new Position(0, 0), 4));
        return game;
    }

    @Test
    void wallsStopThePlayer() {
        Game game = gameOn(".#");
        assertFalse(game.movePlayer(Direction.RIGHT));
        assertFalse(game.movePlayer(Direction.LEFT));
        assertEquals(new Position(0, 0), game.getPlayer().getPosition());
    }

    @Test
    void closedDoorStopsAPlayerWithoutKey() {
        Game game = gameOn(".DE");
        assertFalse(game.movePlayer(Direction.RIGHT));
    }

    @Test
    void keyDoorExitCompletesTheLevel() {
        Game game = gameOn("..DE");
        game.getItems().add(new Key(new Position(1, 0)));
        assertTrue(game.movePlayer(Direction.RIGHT));
        assertEquals(1, game.getPlayer().getKeys());
        assertTrue(game.getItems().isEmpty());
        assertTrue(game.movePlayer(Direction.RIGHT));
        assertFalse(game.isLevelComplete());
        assertTrue(game.movePlayer(Direction.RIGHT));
        assertTrue(game.isLevelComplete());
        assertFalse(game.movePlayer(Direction.LEFT));
    }

    @Test
    void oneMoveLastsOneOverSpeed() {
        Game game = gameOn("..");
        assertEquals(0.25, game.turnSeconds(), 0.0001);
        game.getPlayer().setSpeedBoost(2.0);
        assertEquals(0.125, game.turnSeconds(), 0.0001);
    }

     @Test
    void playerAtFullSpeedOutrunsTheChaser() {
        Game game = gameOn("..........");
        Chaser chaser = new Chaser(new Position(9, 0), 20);
        game.getEnemies().add(chaser);
        for (int i = 0; i < 4; i++) {
            game.movePlayer(Direction.RIGHT);
        }
        // The Chaser no longer moves with the player's turns: it runs in real time.
        assertEquals(new Position(9, 0), chaser.getPosition());
        for (int i = 0; i < 20; i++) {
            game.updateRealTime(0.05); // 20 x 0.05 s = 1 second
        }
        // In 1 second the player made 4 moves, the Chaser only 1.
        assertEquals(new Position(8, 0), chaser.getPosition());
        assertTrue(game.getPlayer().isAlive());
    }

    @Test
    void frozenEnemiesDoNotMove() {
        Game game = gameOn("..........");
        Chaser chaser = new Chaser(new Position(9, 0), 20);
        game.getEnemies().add(chaser);
        game.freezeEnemies(5);
        for (int i = 0; i < 4; i++) {
            game.movePlayer(Direction.RIGHT);
        }
        assertEquals(new Position(9, 0), chaser.getPosition());
        assertTrue(game.isFrozen());
    }

    @Test
    void walkingOnALeechSlowsTheNextTurns() {
        Game game = gameOn("...");
        game.getEnemies().add(new Leech(new Position(1, 0), 10));
        game.movePlayer(Direction.RIGHT);
        assertEquals(0.5, game.turnSeconds(), 0.0001);
    }

    @Test
    void shieldSavesThePlayerFromAChaserHit() {
        Game game = gameOn("....");
        game.getItems().add(new Shield(new Position(1, 0)));
        game.getEnemies().add(new Chaser(new Position(2, 0), 10));
        
        game.movePlayer(Direction.RIGHT);           // picks up the shield
        game.movePlayer(Direction.RIGHT);           // walks into the Chaser
        
        assertTrue(game.getPlayer().isAlive());     // the Chaser hits twice this turn, the shield covers both
        
        // UPDATED FOR 8-SECOND TIMER:
        assertTrue(game.getPlayer().isShielded());  // The shield is still active because it's a timer!
        
        // Manually expire the 8 seconds
        game.getPlayer().tickShield(10.0); 
        assertFalse(game.getPlayer().isShielded());
        
        // Force another hit to ensure the player dies now
        game.movePlayer(Direction.LEFT);
        game.movePlayer(Direction.RIGHT);
        assertFalse(game.getPlayer().isAlive());
    }

    @Test
    void withoutShieldTheChaserKills() {
        Game game = gameOn("...");
        game.getEnemies().add(new Chaser(new Position(1, 0), 10));
        game.movePlayer(Direction.RIGHT);
        assertFalse(game.getPlayer().isAlive());
    }
}