// Owner: Rachid
package com.mazeescape.save;

/**
 * What is saved: the level reached and the maze seed. With these two numbers
 * the game rebuilds exactly the same level.
 *
 * @param level level number, at least 1
 * @param seed  seed of the maze
 */
public record SaveData(int level, long seed) {

    /**
     * Checks the values as soon as the record is created.
     *
     * @throws IllegalArgumentException if the level is below 1
     */
    public SaveData {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be at least 1, got " + level);
        }
    }
}
