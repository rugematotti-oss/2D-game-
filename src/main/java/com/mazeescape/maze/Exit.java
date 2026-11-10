// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.entities.Player;

/** The way out, behind the door. Reaching it completes the level. */
public class Exit extends Tile {

    /** Creates an exit. */
    public Exit() {
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void onEnter(Player player) {
    }

    @Override
    public boolean completesLevel() {
        return true;
    }

    @Override
    public char symbol() {
        return 'E';
    }
}
