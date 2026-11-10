// Owner: Totti & Naim
package com.mazeescape.ui;

import com.mazeescape.core.Game;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.Taser;
import com.mazeescape.entities.items.TimeStop;
import com.mazeescape.maze.LevelManager;

/** Utility class to build the HUD string, keeping it testable without a GUI. */
public class HudText {

    public static String of(Game game) {
        Player p = game.getPlayer();
        StringBuilder sb = new StringBuilder();
        
        // 1. Level & Key
        sb.append("Level ").append(game.getLevel()).append("/").append(LevelManager.MAX_LEVEL);
        sb.append(" | Key: ").append(p.getKeys() > 0 ? "yes" : "no");
        
        // 2. Items
        sb.append(" | Taser [T]: ").append(p.count(Taser.class));
        sb.append(" | Time Stop [F]: ").append(p.count(TimeStop.class));

        // 3. States (Shield, Freeze, Leech) - Exact strings required by tests
        if (p.isShielded()) {
            sb.append(" | SHIELD");
        }
        if (game.isFrozen()) {
            sb.append(" | ENEMIES FROZEN");
        }
        if (isSlowed(game)) {
            sb.append(" | SLOWED BY LEECH");
        }
        
        return sb.toString();
    }

    public static boolean isSlowed(Game game) {
        for (Enemy e : game.getEnemies()) {
            if (e instanceof Leech leech && leech.isAttached()) {
                return true;
            }
        }
        return false;
    }
}