// Owner: Totti
package com.mazeescape;

import com.mazeescape.core.Direction;
import com.mazeescape.core.Game;
import com.mazeescape.maze.LevelManager;
import com.mazeescape.ui.ConsoleRenderer;

import java.io.Console;

public class Main {

    public static void main(String[] args) {
        // 1. Use the real LevelManager to generate a random maze with items and enemies
        LevelManager levels = new LevelManager();
        Game game = levels.newGame();
        
        ConsoleRenderer renderer = new ConsoleRenderer();
        Console console = System.console();

        if (console == null) {
            System.out.println("No interactive console. Run from a real terminal.");
            return;
        }

        renderer.render(game);

        while (game.getPlayer().isAlive() && !game.isLevelComplete()) {
            System.out.print("Move (z/q/s/d) or (t)aser (f)reeze: ");
            String line = console.readLine().trim().toLowerCase();
            
            if (line.equals("t")) {
                game.getPlayer().use(com.mazeescape.entities.items.Taser.class, game);
                continue;
            } else if (line.equals("f")) {
                game.getPlayer().use(com.mazeescape.entities.items.TimeStop.class, game);
                continue;
            }

            Direction d = switch (line) {
                case "z", "w" -> Direction.UP;
                case "s"      -> Direction.DOWN;
                case "q", "a" -> Direction.LEFT;
                case "d"      -> Direction.RIGHT;
                default -> null;
            };
            
            if (d == null) continue;

            // Move player and update game
            game.movePlayer(d);
            
            // Simulate real-time for the console (1 turn = 1 second)
            game.updateRealTime(1.0);
            
            renderer.render(game);
        }
        
        if (game.isLevelComplete()) {
            System.out.println("You found the exit! Level complete.");
        } else {
            System.out.println("Game over. You were caught.");
        }
    }
}