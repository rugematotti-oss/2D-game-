// Owner: Totti
package com.mazeescape.ui;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SpriteFactory {

    private static final int PIXEL_SIZE = 4;
    private static final int GRID = 12;

    // [Direction 0=Down, 1=Left, 2=Right, 3=Up][Frame 0-7]
    private static final Image[][] PLAYER_SPRITES = new Image[4][8];

    public static void loadPlayerSprites() {
        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 8; frame++) {
                PLAYER_SPRITES[dir][frame] = create3DStickman(dir, frame);
            }
        }
    }

    public static Image getPlayerSprite(int direction, int frame) {
        return PLAYER_SPRITES[direction][frame % 8];
    }

    /**
     * Draws one frame of the walking stickman (used by the main menu).
     * 0=Down, 1=Left, 2=Right, 3=Up. The in-game player is drawn live by
     * {@link StickmanRenderer} so it can also breathe and blink when standing still.
     */
    private static Image create3DStickman(int direction, int frameIndex) {
        Canvas canvas = new Canvas(StickmanPose.BOX, StickmanPose.BOX);
        double walkPhase = (frameIndex / 8.0) * (2 * Math.PI);
        StickmanRenderer.draw(canvas.getGraphicsContext2D(),
                StickmanPose.compute(frameIndex * 0.1, walkPhase, true,
                        StickmanPose.Facing.fromIndex(direction)));

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    // --- PIXEL ART ENEMIES ---
    public static Image createChaser() {
        String[] pixels = { "............", "....RRRR....", "...RRRRRR...", "..RRWRRWRR..", "..RRRRRRRR..", "..RRRRRRRR..", "...RRRRRR...", "....RRRR....", "..RR.RR.RR..", "..R...R...R.", "............", "............" };
        return generate(pixels, Color.DARKRED, Color.WHITE);
    }
    public static Image createBomber() {
        String[] pixels = { "............", "............", ".......Y....", "......Y.....", "....OOOO....", "...OORROO...", "..OORRRROO..", "..OOOOOOOO..", "..OOOOOOOO..", "...OOOOOO...", "....YYYY....", "............" };
        return generate(pixels, Color.DARKORANGE, Color.RED);
    }
    public static Image createLeech() {
        String[] pixels = { "............", "............", "............", "....GGGG....", "...GGGGGG...", "..GGWGGWGG..", "..GGGGGGGG..", "..GGGGGGGG..", "...GGGGGG...", "..G.G..G.G..", "............", "............" };
        return generate(pixels, Color.GREEN, Color.WHITE);
    }

    private static Image generate(String[] pattern, Color mainColor, Color secondaryColor) {
        WritableImage image = new WritableImage(GRID * PIXEL_SIZE, GRID * PIXEL_SIZE);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < GRID; y++) {
            String row = pattern[y];
            for (int x = 0; x < GRID; x++) {
                char c = row.charAt(x);
                Color color = null;
                if (c == 'R') color = mainColor;
                else if (c == 'O') color = mainColor;
                else if (c == 'G') color = mainColor;
                else if (c == 'W') color = secondaryColor;
                else if (c == 'Y') color = Color.YELLOW;
                if (color != null) {
                    for (int py = 0; py < PIXEL_SIZE; py++) {
                        for (int px = 0; px < PIXEL_SIZE; px++) {
                            writer.setColor(x * PIXEL_SIZE + px, y * PIXEL_SIZE + py, color);
                        }
                    }
                }
            }
        }
        return image;
    }
}