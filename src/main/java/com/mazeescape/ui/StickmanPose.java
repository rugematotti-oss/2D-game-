// Owner: Rachid
package com.mazeescape.ui;

/**
 * Where every part of the stickman is at a given moment, in a 64 x 64 box
 * (x to the right, y downwards, feet on the ground at y = 56).
 * <p>
 * This class only does maths, no JavaFX, so the animation is unit-tested.
 * {@link StickmanRenderer} turns a pose into a drawing.
 * <ul>
 *   <li>Standing still: the body breathes up and down, the arms sway a little,
 *       the eyes blink every few seconds and the scarf flutters.</li>
 *   <li>Walking: legs and arms swing in opposite directions, the body bobs and the
 *       scarf flies behind him.</li>
 * </ul>
 */
public final class StickmanPose {

    /** Size of the square box the stickman is drawn in, in pixels. */
    public static final double BOX = 64;
    /** Height of the ground line in the box. */
    public static final double GROUND = 56;
    /** Radius of the head. */
    public static final double HEAD_RADIUS = 7;
    /** Number of points of the scarf tail, knot included. */
    public static final int SCARF_POINTS = 4;

    /** Time between two blinks, in seconds. */
    static final double BLINK_PERIOD = 3.2;
    /** How long the eyes stay closed, in seconds. */
    static final double BLINK_LENGTH = 0.15;
    /** Time of one full breath, in seconds. */
    static final double BREATH_PERIOD = 2.4;

    /** Where the stickman looks. */
    public enum Facing {
        /** Towards the player (down the screen). */
        DOWN,
        /** To the left. */
        LEFT,
        /** To the right. */
        RIGHT,
        /** Away from the player (up the screen): only the back of the head shows. */
        UP;

        /**
         * Converts the direction code used by the renderer: 0 down, 1 left, 2 right, 3 up.
         *
         * @param index the code
         * @return the matching facing, DOWN for an unknown code
         */
        public static Facing fromIndex(int index) {
            return switch (index) {
                case 1 -> LEFT;
                case 2 -> RIGHT;
                case 3 -> UP;
                default -> DOWN;
            };
        }
    }

    /**
     * A point of the drawing.
     *
     * @param x horizontal position in the box
     * @param y vertical position in the box
     */
    public record Point(double x, double y) {
    }

    private final Facing facing;
    private final Point head;
    private final Point neck;
    private final Point hip;
    private final Point leftHand;
    private final Point rightHand;
    private final Point leftFoot;
    private final Point rightFoot;
    private final Point[] scarf;
    private final boolean eyesOpen;

    private StickmanPose(Facing facing, Point head, Point neck, Point hip, Point leftHand,
                         Point rightHand, Point leftFoot, Point rightFoot, Point[] scarf,
                         boolean eyesOpen) {
        this.facing = facing;
        this.head = head;
        this.neck = neck;
        this.hip = hip;
        this.leftHand = leftHand;
        this.rightHand = rightHand;
        this.leftFoot = leftFoot;
        this.rightFoot = rightFoot;
        this.scarf = scarf;
        this.eyesOpen = eyesOpen;
    }

    /**
     * Computes the pose.
     *
     * @param time      seconds since the game started, drives breathing, blinking and the scarf
     * @param walkPhase angle of the walk cycle in radians, one full turn is two steps
     * @param moving    true while the stickman walks from one tile to the next
     * @param facing    where he looks
     * @return the pose at that moment
     */
    public static StickmanPose compute(double time, double walkPhase, boolean moving, Facing facing) {
        double swing = moving ? Math.sin(walkPhase) : 0;
        double breath = moving ? 0 : Math.sin(time * 2 * Math.PI / BREATH_PERIOD) * 1.2;
        double bob = moving ? -Math.abs(Math.sin(walkPhase)) * 2 : 0;
        double lift = breath + bob;

        Point head = new Point(32, 15 + lift);
        Point neck = new Point(32, 23 + lift);
        Point hip = new Point(32, 39 + lift * 0.5);

        boolean side = facing == Facing.LEFT || facing == Facing.RIGHT;
        Point leftFoot;
        Point rightFoot;
        Point leftHand;
        Point rightHand;
        if (!moving) {
            double sway = Math.sin(time * 1.7) * 0.8;
            leftFoot = new Point(28, GROUND);
            rightFoot = new Point(36, GROUND);
            leftHand = new Point(25 + sway, 36 + breath);
            rightHand = new Point(39 - sway, 36 + breath);
        } else if (side) {
            leftFoot = new Point(32 - swing * 7, GROUND - Math.max(0, swing) * 3);
            rightFoot = new Point(32 + swing * 7, GROUND - Math.max(0, -swing) * 3);
            leftHand = new Point(32 + swing * 7, 36 + lift);
            rightHand = new Point(32 - swing * 7, 36 + lift);
        } else {
            leftFoot = new Point(28, GROUND - Math.max(0, swing) * 4);
            rightFoot = new Point(36, GROUND - Math.max(0, -swing) * 4);
            leftHand = new Point(24, 35 + swing * 3 + lift);
            rightHand = new Point(40, 35 - swing * 3 + lift);
        }

        return new StickmanPose(facing, head, neck, hip, leftHand, rightHand, leftFoot,
                rightFoot, scarf(neck, time, moving, facing), eyesOpen(time));
    }

    /** The scarf starts at the neck and flies behind, waving more when he runs. */
    private static Point[] scarf(Point neck, double time, boolean moving, Facing facing) {
        double behind = facing == Facing.RIGHT ? -1 : 1;
        double speed = moving ? 12 : 5;
        double amplitude = moving ? 2.5 : 1.2;
        Point[] points = new Point[SCARF_POINTS];
        Point knot = new Point(neck.x(), neck.y() - 1);
        points[0] = knot;
        for (int i = 1; i < SCARF_POINTS; i++) {
            double wave = Math.sin(time * speed - i * 0.9) * amplitude * i * 0.6;
            points[i] = new Point(knot.x() + behind * i * 5, knot.y() + i * 1.5 + wave);
        }
        return points;
    }

    /** Eyes are closed during a short moment at the start of each blink period. */
    static boolean eyesOpen(double time) {
        double inPeriod = (time + 1.0) % BLINK_PERIOD;
        return inPeriod >= BLINK_LENGTH;
    }

    /**
     * Returns where he looks.
     *
     * @return the facing
     */
    public Facing facing() {
        return facing;
    }

    /**
     * Returns the centre of the head.
     *
     * @return the head centre
     */
    public Point head() {
        return head;
    }

    /**
     * Returns the top of the body, where arms and scarf start.
     *
     * @return the neck
     */
    public Point neck() {
        return neck;
    }

    /**
     * Returns the bottom of the body, where the legs start.
     *
     * @return the hip
     */
    public Point hip() {
        return hip;
    }

    /**
     * Returns the left hand.
     *
     * @return the left hand
     */
    public Point leftHand() {
        return leftHand;
    }

    /**
     * Returns the right hand.
     *
     * @return the right hand
     */
    public Point rightHand() {
        return rightHand;
    }

    /**
     * Returns the left foot.
     *
     * @return the left foot
     */
    public Point leftFoot() {
        return leftFoot;
    }

    /**
     * Returns the right foot.
     *
     * @return the right foot
     */
    public Point rightFoot() {
        return rightFoot;
    }

    /**
     * Returns the scarf, from the knot to the tip.
     *
     * @return a copy of the scarf points
     */
    public Point[] scarf() {
        return scarf.clone();
    }

    /**
     * Tells whether the eyes are open (false during a blink).
     *
     * @return true when open
     */
    public boolean eyesOpen() {
        return eyesOpen;
    }

    /**
     * Tells whether the eyes are visible: not when he faces away.
     *
     * @return true unless facing up
     */
    public boolean showsEyes() {
        return facing != Facing.UP;
    }

    /**
     * Horizontal offset of the eyes from the head centre: they look where he goes.
     *
     * @return the offset in pixels
     */
    public double eyeShift() {
        return switch (facing) {
            case LEFT -> -2.5;
            case RIGHT -> 2.5;
            default -> 0;
        };
    }
}
