// Owner: Totti
package com.mazeescape.core;

/**
 * Stores the latest direction requested by the player, read from the console
 * (or later from JavaFX). The game loop consumes it once per tick and clears
 * it, so holding a key does not make the player slide forever.
 */
public class InputHandler {

    private Direction pending;

    public void setDirection(Direction direction) {
        this.pending = direction;
    }

    /** Returns the pending direction once, then clears it. */
    public Direction consume() {
        Direction d = pending;
        pending = null;
        return d;
    }

    public boolean hasPending() {
        return pending != null;
    }
}