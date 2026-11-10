// Owner: Rachid
package com.mazeescape.save;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads and writes the save file, a small text file:
 * <pre>
 * level=2
 * seed=123456789
 * </pre>
 */
public class SaveManager {

    /** Default save file, in the {@code saves/} folder ignored by Git. */
    public static final Path DEFAULT_FILE = Path.of("saves", "save.properties");

    private static final String LEVEL = "level";
    private static final String SEED = "seed";

    private final Path file;

    /**
     * Creates a manager working on a given file. Tests use a temporary one.
     *
     * @param file where the save is stored
     */
    public SaveManager(Path file) {
        this.file = file;
    }

    /** Creates a manager using {@link #DEFAULT_FILE}. */
    public SaveManager() {
        this(DEFAULT_FILE);
    }

    /**
     * Tells whether a save exists.
     *
     * @return true if the save file is there
     */
    public boolean hasSave() {
        return Files.exists(file);
    }

    /**
     * Saves the game, replacing the previous save.
     *
     * @param data what to save
     * @throws SaveException if the file cannot be written
     */
    public void save(SaveData data) throws SaveException {
        Properties props = new Properties();
        props.setProperty(LEVEL, Integer.toString(data.level()));
        props.setProperty(SEED, Long.toString(data.seed()));
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                props.store(out, "Maze Escape save");
            }
        } catch (IOException e) {
            throw new SaveException("Could not write the save file " + file, e);
        }
    }

    /**
     * Reads the save.
     *
     * @return the saved data
     * @throws SaveException if there is no save, or the file is unreadable or broken
     */
    public SaveData load() throws SaveException {
        if (!hasSave()) {
            throw new SaveException("No save file at " + file);
        }
        Properties props = new Properties();
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(in);
        } catch (IOException e) {
            throw new SaveException("Could not read the save file " + file, e);
        }
        try {
            int level = Integer.parseInt(required(props, LEVEL));
            long seed = Long.parseLong(required(props, SEED));
            return new SaveData(level, seed);
        } catch (IllegalArgumentException e) {
            throw new SaveException("The save file is broken: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the save, for example when the player wins the game.
     *
     * @throws SaveException if the file exists but cannot be deleted
     */
    public void delete() throws SaveException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new SaveException("Could not delete the save file " + file, e);
        }
    }

    private String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("missing '" + key + "'");
        }
        return value.trim();
    }
}
