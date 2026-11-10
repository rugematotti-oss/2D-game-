// Owner: Rachid
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

/** Opens the door to the next level. */
public class Key extends Item {

    /**
     * Creates a key.
     *
     * @param position where it lies
     */
    public Key(Position position) {
        super(position);
    }

    @Override
    public void onPickup(Player player, Game game) {
        player.addKey();
    }

    @Override
    public char symbol() {
        return 'K';
    }
}
