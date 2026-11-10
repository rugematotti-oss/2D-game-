// Owner: Both
package com.mazeescape.entities;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;

/** Root of everything that lives in the maze and updates every frame. */
public abstract class GameObject {

    protected Position position;

    protected GameObject(Position position) {
        this.position = position;
    }

    public Position getPosition() { return position; }

    public void setPosition(Position position) { this.position = position; }

    /** Called once per frame. */
    public abstract void update(Game game, double deltaSeconds);
}
