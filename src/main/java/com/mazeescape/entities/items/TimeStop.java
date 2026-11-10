// Owner: Rachid
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

/** Stored on pickup. When used, freezes every enemy for 5 seconds of game time. */
public class TimeStop extends Item implements Usable {

    /** How long enemies stay frozen, in game seconds. */
    public static final double DURATION = 5.0;

    /**
     * Creates a time stop.
     *
     * @param position where it lies
     */
    public TimeStop(Position position) {
        super(position);
    }

    @Override
    public void onPickup(Player player, Game game) {
        player.store(this);
    }

    @Override
    public boolean use(Player player, Game game) {
        game.freezeEnemies(DURATION);
        return true;
    }

    @Override
    public char symbol() {
        return 'Z';
    }
}
