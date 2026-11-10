// Owner: Totti
package com.mazeescape.entities;

import com.mazeescape.core.Direction;
import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.items.Usable;
import com.mazeescape.maze.Maze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The player. No health: one enemy blow kills him, unless a shield is active.
 * <p>
 * Movement is tile by tile. Holding a direction sets the current direction, and 
 * {@link #tickMovement} turns it into a step once enough time has built up for the 
 * current speed.
 */
public class Player extends GameObject {

    private boolean alive = true;
    private int keys;
    private double speed;
    private double speedBoost = 1.0;

    private double shieldSeconds = 0;
    private boolean protectedThisTurn;
    private final List<Usable> inventory = new ArrayList<>();

    /** The direction the player is currently holding down. Null if standing still. */
    private Direction currentDirection = null;

    /** Builds up with time and speed; a step happens each time it reaches 1.0. */
    private double stepProgress = 1.0; // ready to move instantly on the first press

    /**
     * Creates a living player with no key and an empty inventory.
     *
     * @param start the starting tile
     * @param speed base speed, in tiles per second
     */
    public Player(Position start, double speed) {
        super(start);
        this.speed = speed;
    }

    /** The player is driven by input and {@link #tickMovement}, not by this method. */
    @Override
    public void update(Game game, double deltaSeconds) {
        // Movement is driven externally
    }

    /**
     * Moves one tile if the target is inside the maze and not blocking.
     *
     * @param direction where to go
     * @param maze      the maze to move in
     * @return true if the player moved, false if dead, out of bounds, or blocked
     */
    public boolean move(Direction direction, Maze maze) {
        if (!alive) return false;
        Position target = direction.apply(getPosition());
        if (target.x() < 0 || target.y() < 0
                || target.x() >= maze.getWidth()
                || target.y() >= maze.getHeight()) {
            return false;
        }
        if (maze.getTile(target).isBlocking()) {
            return false;
        }
        setPosition(target);
        return true;
    }

    /**
     * Sets the movement direction. Called when a key is pressed.
     *
     * @param d the requested direction
     */
    public void setDirection(Direction d) {
        if (alive) {
            currentDirection = d;
        }
    }

    /**
     * Stops the player from moving. Called when a key is released.
     */
    public void stopMoving() {
        currentDirection = null;
    }

    /**
     * Called about 60 times a second. Adds time to the step timer according to the
     * current speed, then steps in the current direction through {@link Game#movePlayer}. 
     * If blocked by a wall, the step timer resets so the player can immediately turn.
     *
     * @param game         the game that applies the move
     * @param deltaSeconds real seconds since the last call
     */
    public void tickMovement(Game game, double deltaSeconds) {
        if (!alive) {
            currentDirection = null;
            return;
        }

        if (currentDirection == null) {
            stepProgress = 1.0; // reset so the next press is instant
            return;
        }

        stepProgress += getEffectiveSpeed() * deltaSeconds;

        if (stepProgress >= 1.0) {
            if (game.movePlayer(currentDirection)) {
                stepProgress -= 1.0;
            } else {
                // Hit a wall, reset progress but keep direction
                stepProgress = 1.0; 
            }
        }
    }

    /** Kills the player, unless a shield is active. Pending movement is dropped. */
    public void die() {
        if (isShielded()) return;
        if (protectedThisTurn) return;
        alive = false;
        currentDirection = null; // clear input on death
    }

    /**
     * Lets the shield run down.
     *
     * @param deltaSeconds real seconds that passed
     */
    public void tickShield(double deltaSeconds) {
        if (shieldSeconds > 0) {
            shieldSeconds -= deltaSeconds;
            if (shieldSeconds < 0) shieldSeconds = 0;
        }
    }

    /** Called at the start of each step. */
    public void startTurn() { protectedThisTurn = false; }

    /** @return true while the player is alive */
    public boolean isAlive() { return alive; }

    /** Adds one key to the player's keys. */
    public void addKey() { keys++; }

    /** @return the number of keys carried */
    public int getKeys() { return keys; }

    /** @return base speed, in tiles per second, before any boost */
    public double getSpeed() { return speed; }

    /** @param speed new base speed, in tiles per second (the Leech uses it to slow the player) */
    public void setSpeed(double speed) { this.speed = speed; }

    /** @param multiplier speed multiplier from a boost; 1.0 means no boost */
    public void setSpeedBoost(double multiplier) { this.speedBoost = multiplier; }

    /** @return base speed times the boost multiplier, in tiles per second */
    public double getEffectiveSpeed() { return speed * speedBoost; }

    /** @param seconds how long the player is invincible from now (replaces a running shield) */
    public void grantShield(double seconds) { this.shieldSeconds = seconds; }

    /** @return true while the shield protects the player */
    public boolean isShielded() { return shieldSeconds > 0; }

    /**
     * Puts a usable item in the inventory.
     *
     * @param item the item to keep for later
     */
    public void store(Usable item) { inventory.add(item); }

    /**
     * Counts the stored items of a kind.
     *
     * @param type the item class, for example {@code Taser.class}
     * @return how many of them are in the inventory
     */
    public int count(Class<? extends Usable> type) {
        int n = 0;
        for (Usable u : inventory) {
            if (type.isInstance(u)) n++;
        }
        return n;
    }

    /**
     * Uses the first stored item of a kind. The item leaves the inventory only if it
     * reports that it worked, so a Taser with no Leech to remove is kept.
     *
     * @param type the item class to use
     * @param game the running game
     * @return true if an item was used up
     */
    public boolean use(Class<? extends Usable> type, Game game) {
        Iterator<Usable> it = inventory.iterator();
        while (it.hasNext()) {
            Usable u = it.next();
            if (type.isInstance(u)) {
                if (u.use(this, game)) {
                    it.remove();
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}