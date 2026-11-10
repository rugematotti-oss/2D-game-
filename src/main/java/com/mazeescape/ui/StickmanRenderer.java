// Owner: Rachid
package com.mazeescape.ui;

import com.mazeescape.ui.StickmanPose.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Draws a {@link StickmanPose}: black stick body, round white head with dot eyes,
 * a red scarf and white sneakers. Everything is drawn in a 64 x 64 box.
 */
public final class StickmanRenderer {

    private static final Color INK = Color.web("#111111");
    private static final Color SCARF = Color.web("#d62828");
    private static final Color SCARF_DARK = Color.web("#9d0208");
    private static final Color OUTLINE = Color.rgb(235, 235, 235, 0.85);
    private static final double LIMB_WIDTH = 3;

    private StickmanRenderer() {
    }

    /**
     * Draws the stickman. The caller clears the canvas first.
     *
     * @param gc   the pen of a canvas at least 64 x 64
     * @param pose the pose to draw
     */
    public static void draw(GraphicsContext gc, StickmanPose pose) {
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        drawShadow(gc);
        drawScarfTail(gc, pose);
        drawLimbs(gc, pose);
        drawSneaker(gc, pose.leftFoot());
        drawSneaker(gc, pose.rightFoot());
        drawHead(gc, pose);
        drawScarfKnot(gc, pose);
    }

    private static void drawShadow(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillOval(20, StickmanPose.GROUND - 1, 24, 6);
    }

    private static void drawScarfTail(GraphicsContext gc, StickmanPose pose) {
        Point[] s = pose.scarf();
        double[] xs = new double[s.length];
        double[] ys = new double[s.length];
        for (int i = 0; i < s.length; i++) {
            xs[i] = s[i].x();
            ys[i] = s[i].y();
        }
        gc.setStroke(SCARF);
        gc.setLineWidth(4);
        gc.strokePolyline(xs, ys, s.length);
    }

    /** Light outline first, then the black limbs on top: visible on a dark floor. */
    private static void drawLimbs(GraphicsContext gc, StickmanPose pose) {
        strokeBody(gc, pose, OUTLINE, LIMB_WIDTH + 2);
        strokeBody(gc, pose, INK, LIMB_WIDTH);
    }

    private static void strokeBody(GraphicsContext gc, StickmanPose pose, Color color, double width) {
        gc.setStroke(color);
        gc.setLineWidth(width);
        line(gc, pose.hip(), pose.leftFoot());
        line(gc, pose.hip(), pose.rightFoot());
        line(gc, pose.neck(), pose.hip());
        Point shoulder = new Point(pose.neck().x(), pose.neck().y() + 3);
        line(gc, shoulder, pose.leftHand());
        line(gc, shoulder, pose.rightHand());
    }

    private static void drawSneaker(GraphicsContext gc, Point foot) {
        gc.setFill(Color.WHITE);
        gc.fillOval(foot.x() - 4, foot.y() - 2, 8, 4);
        gc.setStroke(INK);
        gc.setLineWidth(1.2);
        gc.strokeOval(foot.x() - 4, foot.y() - 2, 8, 4);
    }

    private static void drawHead(GraphicsContext gc, StickmanPose pose) {
        Point h = pose.head();
        double r = StickmanPose.HEAD_RADIUS;
        gc.setFill(Color.WHITE);
        gc.fillOval(h.x() - r, h.y() - r, 2 * r, 2 * r);
        gc.setStroke(INK);
        gc.setLineWidth(2.2);
        gc.strokeOval(h.x() - r, h.y() - r, 2 * r, 2 * r);

        if (!pose.showsEyes()) {
            return;
        }
        double ex = h.x() + pose.eyeShift();
        double ey = h.y() - 1;
        gc.setFill(INK);
        if (pose.eyesOpen()) {
            gc.fillOval(ex - 3.5, ey - 1.5, 3, 3);
            gc.fillOval(ex + 0.5, ey - 1.5, 3, 3);
        } else {
            gc.setLineWidth(1.4);
            gc.strokeLine(ex - 3.5, ey, ex - 0.5, ey);
            gc.strokeLine(ex + 0.5, ey, ex + 3.5, ey);
        }
    }

    private static void drawScarfKnot(GraphicsContext gc, StickmanPose pose) {
        Point k = pose.scarf()[0];
        gc.setFill(SCARF);
        gc.fillRoundRect(k.x() - 5, k.y() - 2, 10, 4, 3, 3);
        gc.setStroke(SCARF_DARK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(k.x() - 5, k.y() - 2, 10, 4, 3, 3);
    }

    private static void line(GraphicsContext gc, Point a, Point b) {
        gc.strokeLine(a.x(), a.y(), b.x(), b.y());
    }
}
