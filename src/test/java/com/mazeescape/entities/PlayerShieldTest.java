// Owner: Totti
package com.mazeescape.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Position;
import org.junit.jupiter.api.Test;

class PlayerShieldTest {

    @Test
    void shieldBlocksDeath() {
        Player player = new Player(new Position(1, 1), 4.0);
        
        // Grant the 8-second shield
        player.grantShield(8.0);
        assertTrue(player.isShielded());

        // Try to kill the player
        player.die();
        
        // Player should still be alive because of the shield
        assertTrue(player.isAlive(), "Shield should block the killing blow");
    }

    @Test
    void shieldExpiresAfterEightSeconds() {
        Player player = new Player(new Position(1, 1), 4.0);
        player.grantShield(8.0);

        // Simulate 7.9 seconds passing (should still be shielded)
        for (int i = 0; i < 79; i++) {
            player.tickShield(0.1);
        }
        assertTrue(player.isShielded(), "Shield should still be active after 7.9s");

        // Simulate 0.2 more seconds (total 8.1s, should be broken)
        player.tickShield(0.2);
        assertFalse(player.isShielded(), "Shield should break after 8 seconds");
    }

    @Test
    void playerDiesAfterShieldBreaks() {
        Player player = new Player(new Position(1, 1), 4.0);
        player.grantShield(1.0);

        // Tick down the timer until it breaks
        player.tickShield(1.1);
        assertFalse(player.isShielded());

        // Now a killing blow should kill the player
        player.die();
        assertFalse(player.isAlive(), "Player should die after the shield has expired");
    }
}