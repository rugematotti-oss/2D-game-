// Owner: Rachid
package com.mazeescape.save;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SaveManagerTest {

    @TempDir
    Path dir;

    @Test
    void noFileMeansNoSave() {
        SaveManager sm = new SaveManager(dir.resolve("save.properties"));
        assertFalse(sm.hasSave());
        assertThrows(SaveException.class, sm::load);
    }

    @Test
    void whatIsSavedIsLoadedBack() throws SaveException {
        SaveManager sm = new SaveManager(dir.resolve("saves/save.properties"));
        sm.save(new SaveData(2, -123456789L));
        assertTrue(sm.hasSave());
        assertEquals(new SaveData(2, -123456789L), sm.load());
    }

    @Test
    void savingAgainReplacesTheOldSave() throws SaveException {
        SaveManager sm = new SaveManager(dir.resolve("save.properties"));
        sm.save(new SaveData(1, 1));
        sm.save(new SaveData(3, 9));
        assertEquals(new SaveData(3, 9), sm.load());
    }

    @Test
    void brokenFileGivesASaveExceptionNotACrash() throws Exception {
        Path file = dir.resolve("save.properties");
        Files.writeString(file, "level=abc\nseed=1\n");
        SaveException e = assertThrows(SaveException.class, () -> new SaveManager(file).load());
        assertTrue(e.getMessage().contains("broken"));
    }

    @Test
    void missingValueOrLevelZeroGivesASaveException() throws Exception {
        Path file = dir.resolve("save.properties");
        Files.writeString(file, "level=2\n");
        assertThrows(SaveException.class, () -> new SaveManager(file).load());
        Files.writeString(file, "level=0\nseed=1\n");
        assertThrows(SaveException.class, () -> new SaveManager(file).load());
    }

    @Test
    void writingWhereItIsImpossibleGivesASaveException() throws Exception {
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "a file, not a folder");
        SaveManager sm = new SaveManager(blocker.resolve("save.properties"));
        assertThrows(SaveException.class, () -> sm.save(new SaveData(1, 1)));
    }

    @Test
    void deleteRemovesTheSave() throws SaveException {
        SaveManager sm = new SaveManager(dir.resolve("save.properties"));
        sm.save(new SaveData(1, 1));
        sm.delete();
        assertFalse(sm.hasSave());
        sm.delete();
    }

    @Test
    void defaultFileIsInTheSavesFolder() {
        assertEquals(SaveManager.DEFAULT_FILE, Path.of("saves", "save.properties"));
        assertNotNull(new SaveManager());
    }

    @Test
    void saveDataRefusesLevelZero() {
        assertThrows(IllegalArgumentException.class, () -> new SaveData(0, 1));
    }
}
