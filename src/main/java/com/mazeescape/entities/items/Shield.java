// Owner: Naim & Totti
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

/** Makes the player invincible for a few seconds, as soon as it is picked up. */
public class Shield extends Item {

    /** How long the player stays invincible, in seconds. */
    public static final double DURATION = 8.0;

    /**
     * Creates a shield.
     *
     * @param position where it lies
     */
    public Shield(Position position) {
        super(position);
    }

    @Override
    public char symbol() {
        return 'H';
    }

    /** Grants {@link #DURATION} seconds of invincibility. */
    @Override
    public void onPickup(Player player, Game game) {
        player.grantShield(DURATION);
    }
}
