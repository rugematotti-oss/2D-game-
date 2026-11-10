// Owner: Totti
package com.mazeescape.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.mazeescape.core.Position;
import org.junit.jupiter.api.Test;

class SpriteMotionTest {

    @Test
    void slidesInsteadOfJumping() {
        SpriteMotion motion = new SpriteMotion(48);
        motion.update(new Position(0, 0), 0.016, 4.0); 
        
        // Move 1 tile
        motion.update(new Position(1, 0), 0.1, 4.0); 
        
        // It should be sliding, not snapped to 48.0
        assertTrue(motion.x() > 0 && motion.x() < 48.0);
    }

    @Test
    void snapsWhenTeleported() {
        SpriteMotion motion = new SpriteMotion(48);
        motion.update(new Position(0, 0), 0.016, 4.0);
        
        // Pass 1.0s and speed 4.0 to move 1 tile
        motion.update(new Position(1, 0), 1.0, 4.0); 
        
        // With enough time, it should reach exactly 48.0
        assertEquals(48.0, motion.x(), 0.1);
    }
}