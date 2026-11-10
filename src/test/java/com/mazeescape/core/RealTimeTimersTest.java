// Owner: Rachid
package com.mazeescape.core;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Chaser;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.SpeedBoost;
import com.mazeescape.entities.items.TimeStop;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import org.junit.jupiter.api.Test;

/** Timers must run on the real clock: a player standing still cannot pause them. */
class RealTimeTimersTest {

    private Game corridor() {
        Maze maze = new Maze(10, 1);
        for (int x = 0; x < 10; x++) {
            maze.setTile(new Position(x, 0), new Floor());
        }
        Game game = new Game();
        game.setMaze(maze);
        game.setPlayer(new Player(new Position(0, 0), 4));
        return game;
    }

    private void wait(Game game, double seconds) {
        for (double t = 0; t < seconds; t += 0.05) {
            game.updateRealTime(0.05);
        }
    }

    @Test
    void timeStopEndsEvenIfThePlayerStandsStill() {
        Game game = corridor();
        Chaser chaser = new Chaser(new Position(9, 0), 20);
        game.getEnemies().add(chaser);
        new TimeStop(new Position(0, 0)).use(game.getPlayer(), game);

        wait(game, 4.9);
        assertTrue(game.isFrozen());
        assertEquals(new Position(9, 0), chaser.getPosition());

        wait(game, 2.0);
        assertFalse(game.isFrozen());
        assertNotEquals(new Position(9, 0), chaser.getPosition());
    }

    @Test
    void speedBoostEndsEvenIfThePlayerStandsStill() {
        Game game = corridor();
        new SpeedBoost(new Position(0, 0)).onPickup(game.getPlayer(), game);
        assertEquals(6.0, game.getPlayer().getEffectiveSpeed(), 0.001);
        wait(game, 5.2);
        assertEquals(4.0, game.getPlayer().getEffectiveSpeed(), 0.001);
    }

    @Test
    void leechFallsOffAfterTenRealSeconds() {
        Game game = corridor();
        Leech leech = new Leech(new Position(1, 0), 10);
        game.getEnemies().add(leech);
        leech.onPlayerContact(game.getPlayer());
        wait(game, 9.8);
        assertTrue(leech.isAttached());
        wait(game, 0.4);
        assertFalse(leech.isAttached());
        assertEquals(4.0, game.getPlayer().getSpeed(), 0.001);
    }
}
