// Owner: Rachid
package com.mazeescape.entities.items;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import org.junit.jupiter.api.Test;

class ItemTest {

    private final Position here = new Position(1, 0);
    private final Player player = new Player(new Position(0, 0), 4);
    private final Game game = new Game();

    ItemTest() {
        Maze maze = new Maze(5, 1);
        for (int x = 0; x < 5; x++) {
            maze.setTile(new Position(x, 0), new Floor());
        }
        game.setMaze(maze);
        game.setPlayer(player);
    }

    @Test
    void keyGivesAKey() {
        new Key(here).onPickup(player, game);
        assertEquals(1, player.getKeys());
    }

    @Test
    void speedBoostLastsFiveSeconds() {
        new SpeedBoost(here).onPickup(player, game);
        assertEquals(6.0, player.getEffectiveSpeed(), 0.0001);
        game.getEffects().tick(4.9);
        assertEquals(6.0, player.getEffectiveSpeed(), 0.0001);
        game.getEffects().tick(0.2);
        assertEquals(4.0, player.getEffectiveSpeed(), 0.0001);
    }

        @Test
    void shieldAbsorbsOneKill() {
        Player player = new Player(new Position(1, 1), 4.0);
        Shield shield = new Shield(new Position(1, 1));
        shield.onPickup(player, null);
        
        assertTrue(player.isShielded());
        
        player.die();
        assertTrue(player.isAlive());
        
        // NEW: Manually expire the 8-second shield
        player.tickShield(10.0); 
        
        // Now the second hit will kill
        player.die();
        assertFalse(player.isAlive());
    }

    @Test
    void taserIsKeptWhenNoLeechIsAttached() {
        new Taser(here).onPickup(player, game);
        assertEquals(1, player.count(Taser.class));
        assertFalse(player.use(Taser.class, game));
        assertEquals(1, player.count(Taser.class));
    }

    @Test
    void taserRemovesTheLeechAndIsUsedUp() {
        Leech leech = new Leech(new Position(0, 0), 10);
        game.getEnemies().add(leech);
        leech.onPlayerContact(player);
        new Taser(here).onPickup(player, game);
        assertTrue(player.use(Taser.class, game));
        assertFalse(leech.isAttached());
        assertEquals(4.0, player.getSpeed(), 0.0001);
        assertEquals(0, player.count(Taser.class));
    }

    @Test
    void timeStopFreezesEnemies() {
        new TimeStop(here).onPickup(player, game);
        assertEquals(1, player.count(TimeStop.class));
        assertTrue(player.use(TimeStop.class, game));
        assertTrue(game.isFrozen());
        assertEquals(0, player.count(TimeStop.class));
    }

    @Test
    void usingAnItemWeDoNotHaveDoesNothing() {
        assertFalse(player.use(TimeStop.class, game));
    }

    @Test
    void everyItemHasItsLetter() {
        assertEquals('K', new Key(here).symbol());
        assertEquals('S', new SpeedBoost(here).symbol());
        assertEquals('H', new Shield(here).symbol());
        assertEquals('T', new Taser(here).symbol());
        assertEquals('Z', new TimeStop(here).symbol());
    }

    @Test
    void effectTimerRunsActionsOnce() {
        EffectTimer timer = new EffectTimer();
        int[] calls = {0};
        timer.schedule(1.0, () -> calls[0]++);
        assertEquals(1, timer.size());
        timer.tick(0.5);
        assertEquals(0, calls[0]);
        timer.tick(0.5);
        timer.tick(5);
        assertEquals(1, calls[0]);
        assertEquals(0, timer.size());
    }
}
