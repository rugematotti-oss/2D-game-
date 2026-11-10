// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.entities.Player;

/**
 * The locked door in front of the exit. It blocks everybody until the player
 * walks into it holding a key. Once open, it stays open.
 */
public class Door extends Tile {

    private boolean open;

    /** Creates a closed door. */
    public Door() {
    }

    /**
     * Tells whether the door has been opened.
     *
     * @return true once a player with a key walked into it
     */
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isBlocking() {
        return !open;
    }

    /** Opens the door if the player carries at least one key. */
    @Override
    public void beforeEnter(Player player) {
        if (player.getKeys() > 0) {
            open = true;
        }
    }

    @Override
    public void onEnter(Player player) {
    }

    @Override
    public char symbol() {
        return open ? '/' : 'D';
    }
}
