// Owner: Rachid
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.GameObject;
import com.mazeescape.entities.Player;

/** Anything lying in the maze that the player can pick up. Each kind has its own effect. */
public abstract class Item extends GameObject {

    /**
     * Creates an item lying on a tile.
     *
     * @param position where it lies
     */
    protected Item(Position position) {
        super(position);
    }

    /**
     * Effect when the player picks the item up.
     *
     * @param player the player who walked on it
     * @param game   the running game, for effects that last some time
     */
    public abstract void onPickup(Player player, Game game);

    /**
     * Character that represents the item, used by the renderers.
     *
     * @return one letter
     */
    public abstract char symbol();

    /** Items do not move. */
    @Override
    public void update(Game game, double deltaSeconds) {
    }
}
