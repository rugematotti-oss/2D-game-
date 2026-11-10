// Owner: Totti
package com.mazeescape.ui;

import com.mazeescape.core.Position;
import java.util.LinkedList;
import java.util.Queue;

/** Smoothly interpolates a sprite's pixel position tile-by-tile without cutting corners. */
public class SpriteMotion {
    private final int tileSize;
    private double currentX, currentY;
    private double startX, startY, targetX, targetY;
    private double progress = 1.0; // Ready to move immediately
    
    private final Queue<Position> targetQueue = new LinkedList<>();
    private Position currentTarget = null;

    public SpriteMotion(int tileSize) {
        this.tileSize = tileSize;
    }

    public void update(Position newPos, double deltaSeconds, double speed) {
        // 1. Initialize on the first call
        if (currentTarget == null) {
            currentTarget = newPos;
            currentX = newPos.x() * tileSize;
            currentY = newPos.y() * tileSize;
            startX = currentX; 
            startY = currentY;
            targetX = currentX; 
            targetY = currentY;
            progress = 1.0;
            return;
        }

        // 2. Queue the new target if it changed
        if (!currentTarget.equals(newPos)) {
            if (targetQueue.isEmpty() || !targetQueue.peek().equals(newPos)) {
                targetQueue.add(newPos);
            }
        }

        // 3. If we are not moving, check the queue for the next tile
        if (progress >= 1.0 && !targetQueue.isEmpty()) {
            currentTarget = targetQueue.poll();
            startX = currentX;
            startY = currentY;
            targetX = currentTarget.x() * tileSize;
            targetY = currentTarget.y() * tileSize;
            progress = 0;
        }

        // 4. Move towards the current target
        if (progress < 1.0) {
            double distance = Math.hypot(targetX - startX, targetY - startY);
            
            if (distance > 0.001) {
                double step = (speed * deltaSeconds) / (distance / tileSize);
                progress += step;
                if (progress > 1.0) progress = 1.0;
                
                currentX = startX + (targetX - startX) * progress;
                currentY = startY + (targetY - startY) * progress;
            } else {
                currentX = targetX;
                currentY = targetY;
                progress = 1.0;
            }
        }
    }

    public double x() { return currentX; }
    public double y() { return currentY; }
}