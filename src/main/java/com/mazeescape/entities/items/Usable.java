// Owner: Rachid
package com.mazeescape.entities.items;

import com.mazeescape.core.Game;
import com.mazeescape.entities.Player;

/** An item that is stored when picked up and used later on a keypress. */
public interface Usable {

    /**
     * Uses the item.
     *
     * @param player the player using it
     * @param game   the running game
     * @return true if the item worked and must leave the inventory,
     *         false if it had no effect and is kept
     */
    boolean use(Player player, Game game);
}
