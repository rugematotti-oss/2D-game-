// Owner: Totti
package com.mazeescape.entities.enemies;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import org.junit.jupiter.api.Test;

class LeechMergeTest {

    @Test
    void leechSlowsAndDetachesCorrectly() {
        // 1. Setup a player with normal speed 4.0
        Player player = new Player(new Position(1, 1), 4.0);
        Game game = new Game();
        game.setPlayer(player);
        
        Leech leech = new Leech(new Position(1, 1), 3.0);

        // 2. Step on the Leech
        leech.onPlayerContact(player);

        // 3. Verify it attached and slowed the player by 50%
        assertTrue(leech.isAttached(), "Leech should be attached after contact");
        assertEquals(2.0, player.getSpeed(), 0.001, "Player speed should be halved");

        // 4. Simulate 9 turns passing (should still be attached)
        for (int i = 0; i < 9; i++) {
            leech.updateAttachedTime(player, 1.0); // 1.0 second per turn
        }
        assertTrue(leech.isAttached(), "Leech should still be attached after 9 turns");

        // 5. Simulate the 10th turn (should detach and restore speed)
        leech.updateAttachedTime(player, 1.0);
        assertFalse(leech.isAttached(), "Leech should detach after 10 turns");
        assertEquals(4.0, player.getSpeed(), 0.001, "Player speed should be restored");
    }
}