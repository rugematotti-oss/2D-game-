// Owner: Totti
package com.mazeescape.entities.enemies;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

public class Leech extends Enemy {

    private static final double SLOW_FACTOR = 0.5;
    private static final double DRAIN_SECONDS = 10.0;

    private boolean attached;
    private double attachedSeconds;
    private double normalSpeed;
    private Position lastPlayerPosition; // NEW: Keeps track of where the player was

    public Leech(Position position, double detectionRadius) {
        super(position, detectionRadius);
    }

    @Override
    protected void react(Game game, double deltaSeconds) {
        // Empty: The Leech waits in its tile until stepped on.
    }

    @Override
    public void onPlayerContact(Player player) {
        if (attached) return;
        
        attached = true;
        attachedSeconds = 0;
        normalSpeed = player.getSpeed();
        player.setSpeed(normalSpeed * SLOW_FACTOR);
        lastPlayerPosition = player.getPosition(); // Record starting position
    }

    public void updateAttachedTime(Player player, double deltaSeconds) {
        if (!attached) return;
        
        attachedSeconds += deltaSeconds;
        if (attachedSeconds >= DRAIN_SECONDS) {
            detach(player);
        }
    }

    // NEW: Call this in Game.updateRealTime() so the Leech follows the player
    public void followPlayer(Player player) {
        if (!attached) return;

        // If the player moved, the Leech jumps to their old tile
        if (!player.getPosition().equals(lastPlayerPosition)) {
            setPosition(lastPlayerPosition);
            lastPlayerPosition = player.getPosition();
        }
    }

    public void detach(Player player) {
        if (!attached) return;
        attached = false;
        attachedSeconds = 0;
        player.setSpeed(normalSpeed);
    }

    public boolean isAttached() { 
        return attached; 
    }
}