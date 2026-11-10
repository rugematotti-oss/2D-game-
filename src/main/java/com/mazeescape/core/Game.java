// Owner: Both
package com.mazeescape.core;

import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.entities.enemies.Bomber;
import com.mazeescape.entities.enemies.Chaser;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.EffectTimer;
import com.mazeescape.entities.items.Item;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * One running level: the maze, the player, the enemies and the items, plus the rules
 * that connect them.
 * <p>
 * Time has two layers. {@link #updateRealTime} runs about 60 times a second and drives
 * everything with a clock: the player's step timer, the Chaser, the shield, item effects,
 * the enemy freeze, the Bomber's fuse, and the Leech timer. {@link #update} runs once 
 * after each player step and handles what only matters when the player lands on a new 
 * tile: contact checks.
 */
public class Game {

    private Maze maze;
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private int level = 1;
    private final EffectTimer effects = new EffectTimer();
    private double frozenSeconds;
    private boolean levelComplete;

    /** @return the maze of this level */
    public Maze getMaze() { return maze; }

    /** @return the player */
    public Player getPlayer() { return player; }

    /** @return the live list of enemies; adding to it adds an enemy to the level */
    public List<Enemy> getEnemies() { return enemies; }

    /** @return the live list of items still lying in the maze */
    public List<Item> getItems() { return items; }

    /** @return the level number, from 1 */
    public int getLevel() { return level; }

    /** @param maze the maze to play in */
    public void setMaze(Maze maze) { this.maze = maze; }

    /** @param player the player to control */
    public void setPlayer(Player player) { this.player = player; }

    /** @param level the level number, from 1 */
    public void setLevel(int level) { this.level = level; }

    /** @return the timer that ends item effects such as the speed boost */
    public EffectTimer getEffects() { return effects; }

    /** @return true once the player has stepped on the exit */
    public boolean isLevelComplete() { return levelComplete; }

    /** @return true if the player is dead */
    public boolean isGameOver() {
        return player != null && !player.isAlive();
    }

    /**
     * Freezes every enemy: they neither move nor hurt while frozen. A new freeze never
     * shortens one already running.
     *
     * @param seconds how long the freeze lasts, in real seconds
     */
    public void freezeEnemies(double seconds) { frozenSeconds = Math.max(frozenSeconds, seconds); }

    /** @return true while the enemies are frozen */
    public boolean isFrozen() { return frozenSeconds > 0; }

    /**
     * Duration of one player step at the current speed.
     *
     * @return seconds per tile
     */
    public double turnSeconds() {
        return 1.0 / player.getEffectiveSpeed();
    }

    /**
     * Sets the player's movement direction. Called when a key is pressed.
     *
     * @param direction where to go
     */
    public void setPlayerDirection(Direction direction) {
        if (player != null) {
            player.setDirection(direction);
        }
    }

    /**
     * Stops the player from moving. Called when a movement key is released.
     */
    public void stopPlayerMovement() {
        if (player != null) {
            player.stopMoving();
        }
    }

    /**
     * Moves the player one tile. Called by {@link Player#tickMovement} when the step
     * timer is ready. In order: the tile may react before entry (a door opens with a key),
     * the move is tried, the tile reacts on entry, items on that tile are picked up, and
     * stepping on the exit completes the level.
     *
     * @param direction where to go
     * @return true if the player moved, false if blocked, dead, or the level is over
     */
    public boolean movePlayer(Direction direction) {
        if (player == null || maze == null || !player.isAlive() || levelComplete) {
            return false;
        }
        player.startTurn();
        Position target = direction.apply(player.getPosition());
        if (!maze.inBounds(target)) {
            return false;
        }
        Tile tile = maze.getTile(target);
        tile.beforeEnter(player);
        if (!player.move(direction, maze)) {
            return false;
        }
        tile.onEnter(player);
        pickUpItemAt(target);
        if (tile.completesLevel()) {
            levelComplete = true;
            return true;
        }
        update(turnSeconds());
        return true;
    }

    /** Removes every item lying on the position and applies its effect to the player. */
    private void pickUpItemAt(Position position) {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.getPosition().equals(position)) {
                it.remove();
                item.onPickup(player, this);
            }
        }
    }

    /** Placeholder kept for the console version; the JavaFX game starts through the level manager. */
    public void start() { }

    /**
     * Turn-based part, played after each player move: contact checks happen here.
     *
     * @param deltaSeconds game seconds covered by the move
     */
    public void update(double deltaSeconds) {
        if (player == null || maze == null || !player.isAlive()) return;
        if (isFrozen()) return; // frozen enemies neither move nor hurt

        for (Enemy enemy : new ArrayList<>(enemies)) {
            // Skip Chaser, it is updated in updateRealTime
            if (!(enemy instanceof Chaser)) {
                enemy.update(this, deltaSeconds);
            }
        }

        for (Enemy enemy : enemies) {
            if (player.getPosition().equals(enemy.getPosition())) {
                if (enemy instanceof Leech leech && leech.isAttached()) {
                    continue;
                }
                enemy.onPlayerContact(player);
            }
        }
    }

    /**
     * Real-time loop, called about 60 times a second. Order: player step timer, shield,
     * item effects, freeze, Leech timer/follow, Bomber fuse, then the Chaser moves and 
     * the collision with it is checked.
     *
     * @param deltaSeconds real seconds since the last call
     */
    public void updateRealTime(double deltaSeconds) {
        if (player == null || maze == null || !player.isAlive() || levelComplete) return;

        // 1. Tick the player's movement timer
        player.tickMovement(this, deltaSeconds);

        // 2. Tick every timer in real time: shield, item effects, freeze
        player.tickShield(deltaSeconds);
        effects.tick(deltaSeconds);
        
        if (frozenSeconds > 0) {
            frozenSeconds = Math.max(0, frozenSeconds - deltaSeconds);
            return;
        }
        
        // 3. Tick Leech and Bomber logic
        for (Enemy enemy : enemies) {
            if (enemy instanceof Leech leech && leech.isAttached()) {
                leech.updateAttachedTime(player, deltaSeconds);
                leech.followPlayer(player); // Leech follows the player
            }
            if (enemy instanceof Bomber bomber) {
                bomber.tickFuse(this, deltaSeconds); // Bomber ticks its 2-second fuse
            }
        }

        // 4. Move the Chaser
        for (Enemy enemy : enemies) {
            if (enemy instanceof Chaser) {
                enemy.update(this, deltaSeconds);
            }
        }

        // 5. Check Chaser collision
        for (Enemy enemy : enemies) {
            if (enemy instanceof Chaser && player.getPosition().equals(enemy.getPosition())) {
                player.die();
            }
        }
    }
}