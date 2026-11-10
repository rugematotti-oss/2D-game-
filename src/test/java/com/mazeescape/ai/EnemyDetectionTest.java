package com.mazeescape.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.maze.Floor;
import com.mazeescape.maze.Maze;
import com.mazeescape.maze.Wall;
import org.junit.jupiter.api.Test;

class EnemyDetectionTest {

    /** Minimal enemy, only here to test the detection inherited from Enemy. */
    private static class DummyEnemy extends Enemy {
        DummyEnemy(Position position, double radius) {
            super(position, radius);
        }

        @Override
        protected void react(Game game, double deltaSeconds) { }
    }

    // '#' = wall, '.' = floor
    private Maze build(String... rows) {
        Maze maze = new Maze(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                maze.setTile(new Position(x, y), rows[y].charAt(x) == '#' ? new Wall() : new Floor());
            }
        }
        return maze;
    }

    @Test
    void detectsPlayerInRangeWithClearView() {
        Maze maze = build(".....");
        Enemy enemy = new DummyEnemy(new Position(0, 0), 5);
        Player player = new Player(new Position(3, 0), 4);
        assertTrue(enemy.detects(player, maze));
    }

    @Test
    void doesNotDetectPlayerOutOfRange() {
        Maze maze = build(".........");
        Enemy enemy = new DummyEnemy(new Position(0, 0), 3);
        Player player = new Player(new Position(8, 0), 4);
        assertFalse(enemy.detects(player, maze));
    }

    @Test
    void wallBlocksLineOfSight() {
        Maze maze = build("..#..");
        Enemy enemy = new DummyEnemy(new Position(0, 0), 5);
        Player player = new Player(new Position(4, 0), 4);
        assertFalse(enemy.detects(player, maze));
    }

    @Test
    void detectsPlayerOnTheDiagonal() {
        Maze maze = build("...", "...", "...");
        Enemy enemy = new DummyEnemy(new Position(0, 0), 5);
        Player player = new Player(new Position(2, 2), 4);
        assertTrue(enemy.detects(player, maze));
    }
}