// Owner: Totti
package com.mazeescape.entities.enemies;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.GameObject;
import com.mazeescape.entities.Player;
import com.mazeescape.maze.Maze;

public abstract class Enemy extends GameObject {

    protected final double detectionRadius;

    protected Enemy(Position position, double detectionRadius) {
        super(position);
        this.detectionRadius = detectionRadius;
    }

    public double getDetectionRadius() {
        return detectionRadius;
    }

    /** Default speed (tiles per second) used for smooth visual gliding. */
    public double getSpeed() {
        return 1.0; 
    }

    public boolean detects(Player player, Maze maze) {
        Position from = getPosition();
        Position to = player.getPosition();

        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > detectionRadius) {
            return false;
        }
        return hasLineOfSight(maze, from, to);
    }

    private boolean hasLineOfSight(Maze maze, Position from, Position to) {
        int x = from.x();
        int y = from.y();
        int dx = Math.abs(to.x() - x);
        int dy = Math.abs(to.y() - y);
        int stepX = x < to.x() ? 1 : -1;
        int stepY = y < to.y() ? 1 : -1;
        int error = dx - dy;

        while (x != to.x() || y != to.y()) {
            int doubled = 2 * error;
            if (doubled > -dy) {
                error -= dy;
                x += stepX;
            }
            if (doubled < dx) {
                error += dx;
                y += stepY;
            }
            boolean isTarget = (x == to.x() && y == to.y());
            if (!isTarget && maze.getTile(new Position(x, y)).isBlocking()) {
                return false;
            }
        }
        return true;
    }

    protected abstract void react(Game game, double deltaSeconds);

    protected void idle(Game game, double deltaSeconds) { }

    public void onPlayerContact(Player player) { }

    @Override
    public final void update(Game game, double deltaSeconds) {
        if (detects(game.getPlayer(), game.getMaze())) {
            react(game, deltaSeconds);
        } else {
            idle(game, deltaSeconds);
        }
    }
}