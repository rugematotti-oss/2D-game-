// Owner: Totti
package com.mazeescape.entities.behaviors;

import com.mazeescape.ai.Pathfinder;
import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.enemies.Enemy;

import java.util.ArrayList;
import java.util.List;

/** Strategy: follow the shortest route to the player, one tile at a time. */
public class ChaseBehavior implements EnemyBehavior {

    private static final double REPATH_SECONDS = 0.25;

    private final Pathfinder pathfinder = new Pathfinder();
    private final double tilesPerSecond;

    private List<Position> path = new ArrayList<>();
    private double repathTimer = REPATH_SECONDS; 
    private double stepProgress;

    public ChaseBehavior(double tilesPerSecond) {
        this.tilesPerSecond = tilesPerSecond;
    }

    public double getSpeed() {
        return tilesPerSecond;
    }

    @Override
    public void move(Enemy enemy, Game game, double deltaSeconds) {
        repathTimer += deltaSeconds;
        if (repathTimer >= REPATH_SECONDS) {
            recomputePath(enemy, game);
            repathTimer = 0;
        }

        stepProgress += tilesPerSecond * deltaSeconds;
        
        // Move strictly one tile at a time
        while (stepProgress >= 1 && !path.isEmpty()) {
            Position next = path.remove(0);
            
            // DOUBLE STRICT CHECK: Refuse to move if it's a wall
            if (game.getMaze().getTile(next).isBlocking()) {
                System.err.println("CRITICAL ERROR: Chaser tried to move into a wall at " + next.x() + "," + next.y());
                break; // Stop moving this frame
            }
            
            enemy.setPosition(next);
            stepProgress -= 1;
        }
        
        if (path.isEmpty()) {
            stepProgress = 0;
        }
    }

    private void recomputePath(Enemy enemy, Game game) {
        List<Position> found = pathfinder.findPath(
                game.getMaze(), enemy.getPosition(), game.getPlayer().getPosition());
        
        path = new ArrayList<>(found);
        if (!path.isEmpty()) {
            path.remove(0); // Remove the starting tile (we are already there)
        }
    }
}