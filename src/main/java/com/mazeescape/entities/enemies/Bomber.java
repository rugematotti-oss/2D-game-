// Owner: Totti
package com.mazeescape.entities.enemies;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;

public class Bomber extends Enemy {

    private static final double FUSE_SECONDS = 2.0;
    private static final int BLAST_RANGE = 3;
    private double fuseTimer = 0;
    private boolean lit = false;

    public Bomber(Position position, double detectionRadius) {
        super(position, detectionRadius);
    }

    @Override
    protected void react(Game game, double deltaSeconds) {
        // If it sees the player, light the fuse immediately
        if (!lit) {
            lit = true;
            fuseTimer = FUSE_SECONDS;
        }
    }

    /** Called 60 times a second to tick the fuse down. */
    public void tickFuse(Game game, double deltaSeconds) {
        if (!lit) return;

        fuseTimer -= deltaSeconds;
        if (fuseTimer <= 0) {
            explode(game);
            lit = false;
            fuseTimer = 0;
        }
    }

    private void explode(Game game) {
        Player player = game.getPlayer();
        if (!player.isAlive()) return;

        if (isInBlastRange(game, player.getPosition())) {
            player.die();
        }
        // The Bomber dies after exploding (removes itself from the game)
        game.getEnemies().remove(this); 
    }

    private boolean isInBlastRange(Game game, Position target) {
        Position origin = getPosition();
        int dx = target.x() - origin.x();
        int dy = target.y() - origin.y();

        // Not on the same row or column
        if (dx != 0 && dy != 0) return false;
        // Out of range
        if (Math.abs(dx) + Math.abs(dy) > BLAST_RANGE) return false;

        // Check line of sight (walls block the blast)
        int stepX = Integer.signum(dx);
        int stepY = Integer.signum(dy);
        int x = origin.x();
        int y = origin.y();

        while (x != target.x() || y != target.y()) {
            x += stepX;
            y += stepY;
            if (x == target.x() && y == target.y()) break;
            if (game.getMaze().getTile(new Position(x, y)).isBlocking()) {
                return false; // Wall blocks the explosion
            }
        }
        return true;
    }

    @Override
    public void onPlayerContact(Player player) {
        // If the player steps on it, it explodes instantly
        player.die();
    }
}