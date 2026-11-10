// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.entities.Player;

/** Blocks movement and line of sight. */
public class Wall extends Tile {

    /** Creates a wall. */
    public Wall() {
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void onEnter(Player player) {
    }

    @Override
    public char symbol() {
        return '#';
    }
}
