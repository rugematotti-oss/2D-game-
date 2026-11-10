// Owner: Rachid
package com.mazeescape.maze;

import com.mazeescape.entities.Player;

/**
 * One cell of the maze. Every kind of tile answers the same questions in its own way
 * (polymorphism): the game never asks "is this a door?", it asks the tile.
 */
public abstract class Tile {

    /** Constructor for subclasses. */
    protected Tile() {
    }

    /**
     * Tells whether this tile stops movement and line of sight.
     *
     * @return true if neither the player, the enemies nor sight can pass
     */
    public abstract boolean isBlocking();

    /**
     * Called just before the player tries to step on this tile.
     * A door uses it to unlock itself when the player holds a key.
     *
     * @param player the player who wants to come in
     */
    public void beforeEnter(Player player) {
    }

    /**
     * Called once the player stands on this tile.
     *
     * @param player the player who just arrived
     */
    public abstract void onEnter(Player player);

    /**
     * Tells whether standing on this tile finishes the level.
     *
     * @return true only for the exit
     */
    public boolean completesLevel() {
        return false;
    }

    /**
     * Character that represents this tile, used by the renderers.
     *
     * @return one character
     */
    public abstract char symbol();
}
