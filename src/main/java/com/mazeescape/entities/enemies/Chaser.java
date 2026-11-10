// Owner: Totti
package com.mazeescape.entities.enemies;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.behaviors.ChaseBehavior;
import com.mazeescape.entities.behaviors.EnemyBehavior;

/**
 * The hunter. It waits until it sees the player (detection from {@link Enemy}), then
 * follows the shortest path to him through a {@link ChaseBehavior} (Strategy pattern).
 * If it loses sight of the player for {@link #ESCAPE_SECONDS}, it gives up and waits again.
 * Touching the player kills him.
 */
public class Chaser extends Enemy {

    /** Out of sight for this long, the Chaser gives up (GDD: escape rule). */
    public static final double ESCAPE_SECONDS = 15.0;

    private final EnemyBehavior chaseBehavior;
    private final double speed;
    private boolean alerted;
    private double secondsOutOfSight;

    /**
     * Creates a Chaser with the default speed of 1 tile per second.
     *
     * @param position        where it waits
     * @param detectionRadius how far it can see, in tiles
     */
    public Chaser(Position position, double detectionRadius) {
        this(position, detectionRadius, 1.0);
    }

    /**
     * Creates a Chaser.
     *
     * @param position            where it waits
     * @param detectionRadius     how far it can see, in tiles
     * @param speedTilesPerSecond how fast it walks
     */
    public Chaser(Position position, double detectionRadius, double speedTilesPerSecond) {
        super(position, detectionRadius);
        this.chaseBehavior = new ChaseBehavior(speedTilesPerSecond);
        this.speed = speedTilesPerSecond;
    }

    /** @return the walking speed, in tiles per second */
    @Override
    public double getSpeed() {
        return speed;
    }

    /** @return true while the Chaser is hunting the player */
    public boolean isAlerted() {
        return alerted;
    }

    /** Player in sight: become alerted, reset the escape timer and chase. */
    @Override
    protected void react(Game game, double deltaSeconds) {
        alerted = true;
        secondsOutOfSight = 0;
        chase(game, deltaSeconds);
    }

    /** Player not in sight: keep hunting while alerted, until the escape time runs out. */
    @Override
    protected void idle(Game game, double deltaSeconds) {
        if (!alerted) {
            return; // waits until it sees you
        }
        secondsOutOfSight += deltaSeconds;
        if (secondsOutOfSight >= ESCAPE_SECONDS) {
            alerted = false; // the player escaped: back to normal
            secondsOutOfSight = 0;
            return;
        }
        chase(game, deltaSeconds);
    }

    /** Takes one step along the path and kills the player if it lands on him. */
    private void chase(Game game, double deltaSeconds) {
        Player player = game.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        chaseBehavior.move(this, game, deltaSeconds);
        if (getPosition().equals(player.getPosition())) {
            player.die();
        }
    }

    /** Touching the Chaser kills the player (unless he is shielded). */
    @Override
    public void onPlayerContact(Player player) {
        player.die();
    }
}