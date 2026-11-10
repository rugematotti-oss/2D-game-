// Owner: Rachid
package com.mazeescape.entities.enemies;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

/** GDD rule: out of sight for 15 seconds, the Chaser gives up the hunt. */
class ChaserEscapeTest {

    /** "....#...": the wall at x = 4 blocks both sight and the path. */
    private Game gameWithWall(Player player) {
        String row = "....#...";
        Maze maze = new Maze(row.length(), 1);
        for (int x = 0; x < row.length(); x++) {
            maze.setTile(new Position(x, 0), row.charAt(x) == '#' ? new Wall() : new Floor());
        }
        Game game = new Game();
        game.setMaze(maze);
        game.setPlayer(player);
        return game;
    }

    private void wait(Chaser chaser, Game game, double seconds) {
        for (double t = 0; t < seconds; t += 0.05) {
            chaser.update(game, 0.05);
        }
    }

    @Test
    void chaserGivesUpAfterFifteenSecondsOutOfSight() {
        Player player = new Player(new Position(3, 0), 4);
        Game game = gameWithWall(player);
        Chaser chaser = new Chaser(new Position(0, 0), 10, 0.0001);

        chaser.update(game, 0.05);
        assertTrue(chaser.isAlerted(), "the Chaser sees the player");

        player.setPosition(new Position(7, 0)); // hides behind the wall
        wait(chaser, game, 14.8);
        assertTrue(chaser.isAlerted(), "still hunting before 15 s");

        wait(chaser, game, 0.4);
        assertFalse(chaser.isAlerted(), "gives up after 15 s");
    }

    @Test
    void seeingThePlayerAgainRestartsTheHunt() {
        Player player = new Player(new Position(3, 0), 4);
        Game game = gameWithWall(player);
        Chaser chaser = new Chaser(new Position(0, 0), 10, 0.0001);
        chaser.update(game, 0.05);
        player.setPosition(new Position(7, 0));
        wait(chaser, game, 16);
        assertFalse(chaser.isAlerted());

        player.setPosition(new Position(2, 0));
        chaser.update(game, 0.05);
        assertTrue(chaser.isAlerted());
    }
}
