// Owner: Totti
package com.mazeescape.ui;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.*;
import com.mazeescape.entities.items.Item;
import com.mazeescape.maze.Maze;

public class ConsoleRenderer {

    public void render(Game game) {
        Maze maze = game.getMaze();
        Player player = game.getPlayer();

        StringBuilder sb = new StringBuilder();
        sb.append("Level ").append(game.getLevel())
          .append(" | keys: ").append(player.getKeys())
          .append(" | alive: ").append(player.isAlive())
          .append(" | Speed: ").append(player.getEffectiveSpeed())
          .append('\n');

        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                sb.append(symbolAt(game, new Position(x, y)));
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    private char symbolAt(Game game, Position p) {
        if (game.getPlayer().getPosition().equals(p)) {
            return 'P';
        }
        for (Enemy e : game.getEnemies()) {
            if (e.getPosition().equals(p)) {
                return symbolFor(e);
            }
        }
        for (Item item : game.getItems()) {
            if (item.getPosition().equals(p)) {
                return item.symbol(); // K, S, T, H, Z
            }
        }
        // Use the tile's own symbol for walls, doors, exit, floors
        return game.getMaze().getTile(p).symbol();
    }

    private char symbolFor(Enemy e) {
        if (e instanceof Chaser) return 'C';
        if (e instanceof Bomber) return 'B';
        if (e instanceof Leech)  return 'L';
        return '?';
    }
}