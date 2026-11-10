// Owner: Rachid
package com.mazeescape.maze;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import org.junit.jupiter.api.Test;

class TileTest {

    private final Player player = new Player(new Position(1, 1), 4);

    @Test
    void wallBlocksAndFloorDoesNot() {
        assertTrue(new Wall().isBlocking());
        assertFalse(new Floor().isBlocking());
        assertEquals('#', new Wall().symbol());
        assertEquals('.', new Floor().symbol());
    }

    @Test
    void doorStaysClosedWithoutKey() {
        Door door = new Door();
        door.beforeEnter(player);
        assertTrue(door.isBlocking());
        assertFalse(door.isOpen());
        assertEquals('D', door.symbol());
    }

    @Test
    void doorOpensForAPlayerWithAKey() {
        Door door = new Door();
        player.addKey();
        door.beforeEnter(player);
        assertTrue(door.isOpen());
        assertFalse(door.isBlocking());
        assertEquals('/', door.symbol());
    }

    @Test
    void onlyTheExitCompletesTheLevel() {
        assertTrue(new Exit().completesLevel());
        assertFalse(new Exit().isBlocking());
        assertFalse(new Floor().completesLevel());
        assertFalse(new Door().completesLevel());
        assertFalse(new Wall().completesLevel());
    }

    @Test
    void enteringTilesHasNoSideEffect() {
        new Floor().onEnter(player);
        new Wall().onEnter(player);
        new Door().onEnter(player);
        new Exit().onEnter(player);
        assertTrue(player.isAlive());
    }
}
