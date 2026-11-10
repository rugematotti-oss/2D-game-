// Owner: Naim & Totti
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.entities.enemies.Leech;

public class Taser extends Item implements Usable {

    public Taser(Position position) {
        super(position);
    }

    @Override
    public char symbol() {
        return 'T';
    }

    /** When the player picks it up, it goes into their inventory. */
    @Override
    public void onPickup(Player player, Game game) {
        player.store(this);
    }

    /** When the player presses 'T', zap any attached Leech. */
    @Override
    public boolean use(Player player, Game game) {
        boolean used = false;
        
        // Loop through all enemies in the game
        for (Enemy enemy : game.getEnemies()) {
            // If we find a Leech that is currently attached to the player...
            if (enemy instanceof Leech leech && leech.isAttached()) {
                // ZAP IT!
                leech.detach(player);
                used = true;
            }
        }
        
        // Return true if we zapped a leech (this consumes the Taser).
        // Return false if there was no leech (the player keeps the Taser).
        return used;
    }
}