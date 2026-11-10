// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.entities.Player;

/** Walkable corridor: nothing happens on it. */
public class Floor extends Tile {

    /** Creates a floor tile. */
    public Floor() {
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void onEnter(Player player) {
    }

    @Override
    public char symbol() {
        return '.';
    }
}
