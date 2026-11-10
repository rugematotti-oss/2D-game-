// Owner: Totti
package com.mazeescape.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InputHandlerTest {

    @Test
    void consumeReturnsAndClearsPendingDirection() {
        InputHandler input = new InputHandler();
        input.setDirection(Direction.RIGHT);

        assertEquals(Direction.RIGHT, input.consume());
        assertNull(input.consume());
    }

    @Test
    void hasPendingReflectsState() {
        InputHandler input = new InputHandler();
        assertFalse(input.hasPending());
        input.setDirection(Direction.UP);
        assertTrue(input.hasPending());
        input.consume();
        assertFalse(input.hasPending());
    }

    @Test
    void lastDirectionWins() {
        InputHandler input = new InputHandler();
        input.setDirection(Direction.UP);
        input.setDirection(Direction.LEFT);
        assertEquals(Direction.LEFT, input.consume());
    }
}