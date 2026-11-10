// Owner: Rachid
package com.mazeescape.maze;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.enemies.Enemy;
import com.mazeescape.entities.items.Item;
import com.mazeescape.entities.items.Key;
import com.mazeescape.save.SaveData;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LevelManagerTest {

    @Test
    void newGameStartsAtLevelOneWithEverything() {
        LevelManager lm = new LevelManager(new Random(1));
        Game game = lm.newGame();
        assertEquals(1, game.getLevel());
        assertEquals(13, game.getMaze().getWidth());
        assertEquals(game.getMaze().getStart(), game.getPlayer().getPosition());
        assertEquals(3, game.getEnemies().size());
        assertEquals(5, game.getItems().size());
        assertEquals(1L, game.getItems().stream().filter(i -> i instanceof Key).count());
    }

    @Test
    void nothingIsStackedAndEnemiesAreFarFromTheStart() {
        for (int seed = 0; seed < 20; seed++) {
            LevelManager lm = new LevelManager(new Random(seed));
            Game game = lm.load(new SaveData(3, seed));
            int[][] d = game.getMaze().distancesFrom(game.getMaze().getStart());
            Set<Position> used = new HashSet<>();
            used.add(game.getPlayer().getPosition());
            for (Enemy e : game.getEnemies()) {
                assertTrue(used.add(e.getPosition()), "two things on " + e.getPosition());
                assertTrue(d[e.getPosition().y()][e.getPosition().x()] >= 8);
            }
            for (Item i : game.getItems()) {
                assertTrue(used.add(i.getPosition()), "two things on " + i.getPosition());
                assertTrue(d[i.getPosition().y()][i.getPosition().x()] > 0, "unreachable item");
            }
        }
    }

    @Test
    void restartKeepsTheMazeAndMovesTheKey() {
        LevelManager lm = new LevelManager(new Random(1));
        Game before = lm.newGame();
        Position keyBefore = lm.getKeyPosition();
        Game after = lm.restart();
        assertEquals(before.getMaze().toAscii(), after.getMaze().toAscii());
        assertEquals(before.getEnemies().get(0).getPosition(), after.getEnemies().get(0).getPosition());
        assertNotEquals(keyBefore, lm.getKeyPosition());
    }

    @Test
    void levelsGetBiggerAndHarderUntilTheLast() {
        LevelManager lm = new LevelManager(new Random(1));
        lm.newGame();
        Game two = lm.nextLevel();
        assertEquals(2, two.getLevel());
        assertEquals(15, two.getMaze().getWidth());
        assertEquals(4, two.getEnemies().size());
        Game three = lm.nextLevel();
        assertEquals(17, three.getMaze().getWidth());
        assertEquals(5, three.getEnemies().size());
        assertFalse(lm.hasNextLevel());
        assertThrows(IllegalStateException.class, lm::nextLevel);
    }

    @Test
    void saveDataRebuildsTheSameLevel() {
        LevelManager lm = new LevelManager(new Random(1));
        lm.newGame();
        Game original = lm.nextLevel();
        SaveData data = lm.toSaveData();
        Game loaded = new LevelManager(new Random(99)).load(data);
        assertEquals(2, loaded.getLevel());
        assertEquals(original.getMaze().toAscii(), loaded.getMaze().toAscii());
    }

    @Test
    void impossibleLevelsAreRefused() {
        LevelManager lm = new LevelManager(new Random(1));
        assertThrows(IllegalArgumentException.class, () -> lm.load(new SaveData(4, 1)));
        assertThrows(IllegalStateException.class, lm::restart);
        assertThrows(IllegalStateException.class, lm::toSaveData);
        assertEquals(0, lm.getLevel());
    }
}
