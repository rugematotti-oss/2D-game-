// Owner: Rachid
package com.mazeescape.ui;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.ui.StickmanPose.Facing;
import com.mazeescape.ui.StickmanPose.Point;
import org.junit.jupiter.api.Test;

class StickmanPoseTest {

    @Test
    void standingStillHeBreathes() {
        double y0 = StickmanPose.compute(0.0, 0, false, Facing.DOWN).head().y();
        double y1 = StickmanPose.compute(0.6, 0, false, Facing.DOWN).head().y();
        assertNotEquals(y0, y1, "the head moves with the breath");
        assertTrue(Math.abs(y1 - y0) <= 2.5, "but only a little");
    }

    @Test
    void standingStillHisFeetStayOnTheGround() {
        for (double t = 0; t < 5; t += 0.37) {
            StickmanPose p = StickmanPose.compute(t, 0, false, Facing.LEFT);
            assertEquals(StickmanPose.GROUND, p.leftFoot().y(), 1e-9);
            assertEquals(StickmanPose.GROUND, p.rightFoot().y(), 1e-9);
        }
    }

    @Test
    void walkingLiftsOneFootAtATime() {
        StickmanPose p = StickmanPose.compute(0, Math.PI / 2, true, Facing.RIGHT);
        boolean leftUp = p.leftFoot().y() < StickmanPose.GROUND;
        boolean rightUp = p.rightFoot().y() < StickmanPose.GROUND;
        assertTrue(leftUp ^ rightUp, "exactly one foot is in the air");
    }

    @Test
    void armsSwingOppositeToLegs() {
        StickmanPose p = StickmanPose.compute(0, Math.PI / 2, true, Facing.RIGHT);
        assertTrue(p.leftFoot().x() < 32 && p.leftHand().x() > 32);
    }

    @Test
    void heBlinksSometimesButHisEyesAreMostlyOpen() {
        int closed = 0;
        int total = 0;
        for (double t = 0; t < StickmanPose.BLINK_PERIOD; t += 0.01) {
            total++;
            if (!StickmanPose.eyesOpen(t)) {
                closed++;
            }
        }
        assertTrue(closed > 0, "he blinks");
        assertTrue(closed < total / 10, "but briefly");
    }

    @Test
    void facingAwayHidesTheEyes() {
        assertFalse(StickmanPose.compute(0, 0, false, Facing.UP).showsEyes());
        assertTrue(StickmanPose.compute(0, 0, false, Facing.DOWN).showsEyes());
    }

    @Test
    void eyesLookWhereHeGoes() {
        assertTrue(StickmanPose.compute(0, 0, false, Facing.LEFT).eyeShift() < 0);
        assertTrue(StickmanPose.compute(0, 0, false, Facing.RIGHT).eyeShift() > 0);
        assertEquals(0, StickmanPose.compute(0, 0, false, Facing.DOWN).eyeShift(), 1e-9);
    }

    @Test
    void scarfFliesBehindHim() {
        Point[] right = StickmanPose.compute(1, 0, true, Facing.RIGHT).scarf();
        Point[] left = StickmanPose.compute(1, 0, true, Facing.LEFT).scarf();
        assertEquals(StickmanPose.SCARF_POINTS, right.length);
        assertTrue(right[right.length - 1].x() < right[0].x(), "facing right, the scarf goes left");
        assertTrue(left[left.length - 1].x() > left[0].x(), "facing left, the scarf goes right");
    }

    @Test
    void scarfWavesEvenWhenHeStandsStill() {
        double tip0 = StickmanPose.compute(0.0, 0, false, Facing.DOWN).scarf()[3].y();
        double tip1 = StickmanPose.compute(0.3, 0, false, Facing.DOWN).scarf()[3].y();
        assertNotEquals(tip0, tip1);
    }

    @Test
    void directionCodesMatchTheRenderer() {
        assertEquals(Facing.DOWN, Facing.fromIndex(0));
        assertEquals(Facing.LEFT, Facing.fromIndex(1));
        assertEquals(Facing.RIGHT, Facing.fromIndex(2));
        assertEquals(Facing.UP, Facing.fromIndex(3));
        assertEquals(Facing.DOWN, Facing.fromIndex(42));
    }

    @Test
    void everythingStaysInsideTheBox() {
        for (Facing f : Facing.values()) {
            for (double t = 0; t < 4; t += 0.1) {
                StickmanPose p = StickmanPose.compute(t, t * 5, t > 2, f);
                Point[] all = {p.head(), p.neck(), p.hip(), p.leftHand(), p.rightHand(),
                    p.leftFoot(), p.rightFoot(), p.scarf()[3]};
                for (Point pt : all) {
                    assertTrue(pt.x() > 0 && pt.x() < StickmanPose.BOX, "x out of box: " + pt);
                    assertTrue(pt.y() > 0 && pt.y() < StickmanPose.BOX, "y out of box: " + pt);
                }
            }
        }
    }
}
