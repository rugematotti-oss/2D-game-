// Owner: Rachid
package com.mazeescape.save;

/**
 * Thrown when the game cannot be saved or loaded. It is a checked exception:
 * the compiler forces the caller to handle it, so a broken save never crashes the game.
 */
public class SaveException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what went wrong
     */
    public SaveException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the original error.
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
