// Owner: Rachid
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

/** Makes the player 50% faster for 5 seconds of game time. No stamina meter. */
public class SpeedBoost extends Item {

    /** Speed multiplier while the boost lasts. */
    public static final double MULTIPLIER = 1.5;
    /** How long the boost lasts, in game seconds. */
    public static final double DURATION = 5.0;

    /**
     * Creates a speed boost.
     *
     * @param position where it lies
     */
    public SpeedBoost(Position position) {
        super(position);
    }

    @Override
    public void onPickup(Player player, Game game) {
        player.setSpeedBoost(MULTIPLIER);
        game.getEffects().schedule(DURATION, () -> player.setSpeedBoost(1.0));
    }

    @Override
    public char symbol() {
        return 'S';
    }
}
