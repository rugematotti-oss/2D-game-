// Owner: Totti & Naim
package com.mazeescape.ui;

import com.mazeescape.entities.GameObject;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.*;
import com.mazeescape.entities.items.Item;
import com.mazeescape.maze.Maze;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.shape.Line;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class JavaFxRenderer {

    private static final int TILE_SIZE = 48;

    private final Pane gamePane;
    private final Pane overlayPane;
    
    private final Image chaserSprite;
    private final Image bomberSprite;
    private final Image leechSprite;
    
    private final Label hudLabel;
    private final MazeRenderer mazeRenderer = new MazeRenderer();
    private final Canvas mazeCanvas = new Canvas();
    private final Map<GameObject, SpriteMotion> motions = new IdentityHashMap<>();
    
    private long lastFrameNanos;
    private double frameSeconds;
    
    private int playerDirection = 0;
    private double playerClock;
    private double playerWalkPhase;
    private final Canvas playerCanvas = new Canvas(StickmanPose.BOX, StickmanPose.BOX);
    private Position lastPlayerPos;
    
    private Runnable onRestartListener;
    private Runnable onNewGameListener;
    
    private VBox gameOverOverlay;
    private VBox victoryOverlay;

    public JavaFxRenderer(Pane gamePane, Pane overlayPane) {
        this.gamePane = gamePane;
        this.overlayPane = overlayPane;
        
        SpriteFactory.loadPlayerSprites();
        this.chaserSprite = SpriteFactory.createChaser();
        this.bomberSprite = SpriteFactory.createBomber();
        this.leechSprite  = SpriteFactory.createLeech();

        this.hudLabel = new Label();
        this.hudLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5px;");
        this.hudLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        this.hudLabel.setTranslateX(10);
        this.hudLabel.setTranslateY(10);
    }

    public void setOnRestartListener(Runnable listener) { this.onRestartListener = listener; }
    public void setOnNewGameListener(Runnable listener) { this.onNewGameListener = listener; }

    public void reset() {
        motions.clear();
        lastFrameNanos = 0;
        gameOverOverlay = null;
        victoryOverlay = null;
        overlayPane.getChildren().clear();
        lastPlayerPos = null;
        playerDirection = 0;
        playerWalkPhase = 0;
    }

    public void render(Game game) {
        updateFrameTime();
        removeStaleMotions(game);

        gamePane.getChildren().clear();

        drawMaze(game.getMaze());
        drawItems(game);
        drawEnemies(game);
        drawPlayer(game.getPlayer());

        Player p = game.getPlayer();
        
        // One source of truth for the HUD text: HudText (tested without a window).
        String hudText = HudText.of(game);
        boolean isSlowed = HudText.isSlowed(game);

        if (isSlowed) {
            hudLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(255, 0, 0, 0.8); -fx-padding: 5px; -fx-font-weight: bold;");
        } else if (p.isShielded()) {
            hudLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0, 191, 255, 0.8); -fx-padding: 5px; -fx-font-weight: bold;");
        } else {
            hudLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5px;");
        }
        
        hudLabel.setText(hudText);
        gamePane.getChildren().add(hudLabel);

        if (!p.isAlive()) {
            if (gameOverOverlay == null) {
                gameOverOverlay = buildGameOverOverlay(game);
                overlayPane.getChildren().add(gameOverOverlay);
            }
        } else if (game.isLevelComplete()) {
            if (victoryOverlay == null) {
                victoryOverlay = buildVictoryOverlay(game);
                overlayPane.getChildren().add(victoryOverlay);
            }
        } else {
            if (gameOverOverlay != null) {
                overlayPane.getChildren().remove(gameOverOverlay);
                gameOverOverlay = null;
            }
            if (victoryOverlay != null) {
                overlayPane.getChildren().remove(victoryOverlay);
                victoryOverlay = null;
            }
        }
    }

    private void updateFrameTime() {
        long now = System.nanoTime();
        frameSeconds = lastFrameNanos == 0 ? 0 : Math.min((now - lastFrameNanos) / 1_000_000_000.0, 0.1);
        lastFrameNanos = now;
    }

    private void removeStaleMotions(Game game) {
        Set<GameObject> onScreen = Collections.newSetFromMap(new IdentityHashMap<>());
        onScreen.add(game.getPlayer());
        onScreen.addAll(game.getEnemies());
        motions.keySet().retainAll(onScreen);
    }

    private void drawPlayer(Player player) {
        if (!player.isAlive()) return;

        Position currentPos = player.getPosition();
        if (lastPlayerPos != null && !currentPos.equals(lastPlayerPos)) {
            int dx = currentPos.x() - lastPlayerPos.x();
            int dy = currentPos.y() - lastPlayerPos.y();
            if (Math.abs(dx) > Math.abs(dy)) {
                playerDirection = (dx > 0) ? 2 : 1;
            } else {
                playerDirection = (dy > 0) ? 0 : 3;
            }
        }
        lastPlayerPos = currentPos;

        SpriteMotion motion = motions.computeIfAbsent(player, o -> new SpriteMotion(TILE_SIZE));
        motion.update(player.getPosition(), frameSeconds, player.getEffectiveSpeed());

        double distToTargetX = (player.getPosition().x() * TILE_SIZE) - motion.x();
        double distToTargetY = (player.getPosition().y() * TILE_SIZE) - motion.y();
        boolean isMoving = Math.abs(distToTargetX) > 1 || Math.abs(distToTargetY) > 1;

        // The clock always runs (breathing, blinking, scarf); the walk cycle only while moving.
        playerClock += frameSeconds;
        if (isMoving) {
            playerWalkPhase += frameSeconds * player.getEffectiveSpeed() * Math.PI;
        }

        // 1. Draw Player Shield Aura
        if (player.isShielded()) {
            double cx = motion.x() + (TILE_SIZE / 2.0);
            double cy = motion.y() + (TILE_SIZE / 2.0);

            Circle aura = new Circle(cx, cy, TILE_SIZE / 1.5);
            aura.setFill(Color.rgb(0, 191, 255, 0.15));
            aura.setStroke(Color.CYAN);
            aura.setStrokeWidth(2);
            aura.setEffect(new Glow(0.6));
            gamePane.getChildren().add(aura);
        }

        // 2. Draw the animated stickman, redrawn every frame
        GraphicsContext gc = playerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, StickmanPose.BOX, StickmanPose.BOX);
        StickmanRenderer.draw(gc, StickmanPose.compute(playerClock, playerWalkPhase, isMoving,
                StickmanPose.Facing.fromIndex(playerDirection)));
        playerCanvas.setLayoutX(motion.x() - (StickmanPose.BOX - TILE_SIZE) / 2);
        playerCanvas.setLayoutY(motion.y() - (StickmanPose.BOX - TILE_SIZE));
        gamePane.getChildren().add(playerCanvas);
    }

    private void drawMaze(Maze maze) {
        double width = maze.getWidth() * TILE_SIZE;
        double height = maze.getHeight() * TILE_SIZE;

        if (mazeCanvas.getWidth() != width || mazeCanvas.getHeight() != height) {
            mazeCanvas.setWidth(width);
            mazeCanvas.setHeight(height);
        }

        GraphicsContext gc = mazeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        mazeRenderer.render(gc, maze, TILE_SIZE, 0, 0);

        gamePane.getChildren().add(mazeCanvas);
    }

    private void drawItems(Game game) {
        for (Item item : game.getItems()) {
            double cx = item.getPosition().x() * TILE_SIZE + TILE_SIZE / 2.0;
            double cy = item.getPosition().y() * TILE_SIZE + TILE_SIZE / 2.0;
            double r = TILE_SIZE / 3.0;

            switch (item.symbol()) {
                case 'K' -> { 
                    Circle keyHead = new Circle(cx, cy - 6, 8, Color.GOLD);
                    keyHead.setStroke(Color.BLACK);
                    keyHead.setStrokeWidth(1.5);
                    Circle keyHole = new Circle(cx, cy - 6, 3, Color.web("#2c3e50"));
                    keyHole.setStroke(Color.BLACK);
                    keyHole.setStrokeWidth(1.5);
                    Rectangle keyShaft = new Rectangle(cx - 2, cy + 2, 4, 14);
                    keyShaft.setFill(Color.GOLD);
                    keyShaft.setStroke(Color.BLACK);
                    keyShaft.setStrokeWidth(1.5);
                    Rectangle tooth1 = new Rectangle(cx + 2, cy + 8, 6, 4);
                    tooth1.setFill(Color.GOLD);
                    tooth1.setStroke(Color.BLACK);
                    tooth1.setStrokeWidth(1.5);
                    Rectangle tooth2 = new Rectangle(cx + 2, cy + 14, 4, 3);
                    tooth2.setFill(Color.GOLD);
                    tooth2.setStroke(Color.BLACK);
                    tooth2.setStrokeWidth(1.5);
                    gamePane.getChildren().addAll(keyHead, keyHole, keyShaft, tooth1, tooth2);
                }
                case 'T' -> { 
                    Rectangle handle = new Rectangle(cx - 8, cy + 4, 10, 16);
                    handle.setFill(Color.DARKGRAY);
                    handle.setStroke(Color.BLACK);
                    handle.setArcWidth(4);
                    handle.setArcHeight(4);
                    Rectangle body = new Rectangle(cx - 14, cy - 6, 24, 12);
                    body.setFill(Color.DARKGRAY);
                    body.setStroke(Color.BLACK);
                    body.setArcWidth(6);
                    body.setArcHeight(6);
                    Line prongTop = new Line(cx + 8, cy - 2, cx + 16, cy - 6);
                    prongTop.setStroke(Color.SILVER);
                    prongTop.setStrokeWidth(2);
                    Line prongBottom = new Line(cx + 8, cy + 4, cx + 16, cy + 8);
                    prongBottom.setStroke(Color.SILVER);
                    prongBottom.setStrokeWidth(2);
                    Polygon spark = new Polygon();
                    spark.getPoints().addAll(
                        cx + 13.0, cy - 4.0, cx + 15.0, cy - 1.0, cx + 12.0, cy + 1.0,
                        cx + 16.0, cy + 4.0, cx + 14.0, cy + 1.0, cx + 16.0, cy - 1.0
                    );
                    spark.setFill(Color.YELLOW);
                    spark.setStroke(Color.ORANGE);
                    gamePane.getChildren().addAll(handle, body, prongTop, prongBottom, spark);
                }
                case 'S' -> { 
                    Polygon bolt = new Polygon();
                    bolt.getPoints().addAll(
                        cx + 6.0, cy - 18.0, cx - 8.0, cy + 2.0, cx - 2.0, cy + 2.0,
                        cx - 6.0, cy + 18.0, cx + 8.0, cy - 2.0, cx + 2.0, cy - 2.0
                    );
                    bolt.setFill(Color.web("#FFD700")); 
                    bolt.setStroke(Color.web("#FF8C00")); 
                    bolt.setStrokeWidth(2.5);
                    Glow glow = new Glow(0.8);
                    bolt.setEffect(glow);
                    gamePane.getChildren().add(bolt);
                }
                case 'H' -> { 
                    // Left Half: Deep Blue
                    Polygon leftShield = new Polygon();
                    leftShield.getPoints().addAll(
                        cx, cy - 16.0,        // Top Center
                        cx, cy + 16.0,        // Bottom Center
                        cx - 12.0, cy + 4.0,  // Mid Left
                        cx - 12.0, cy - 10.0  // Top Left
                    );
                    leftShield.setFill(Color.web("#003366")); // Deep Blue
                    leftShield.setStroke(Color.web("#00BFFF")); // Cyan outline
                    leftShield.setStrokeWidth(2);

                    // Right Half: Light Blue
                    Polygon rightShield = new Polygon();
                    rightShield.getPoints().addAll(
                        cx, cy - 16.0,        // Top Center
                        cx + 12.0, cy - 10.0, // Top Right
                        cx + 12.0, cy + 4.0,  // Mid Right
                        cx, cy + 16.0         // Bottom Center
                    );
                    rightShield.setFill(Color.web("#87CEEB")); // Light Blue (Sky Blue)
                    rightShield.setStroke(Color.web("#00BFFF"));
                    rightShield.setStrokeWidth(2);

                    // Glow effect
                    Glow glow = new Glow(0.6);
                    leftShield.setEffect(glow);
                    rightShield.setEffect(glow);

                    // Center split line
                    Line splitLine = new Line(cx, cy - 16.0, cx, cy + 16.0);
                    splitLine.setStroke(Color.WHITE);
                    splitLine.setStrokeWidth(2);

                    // Inner detail cross (White)
                    Line vertical = new Line(cx, cy - 8, cx, cy + 10);
                    vertical.setStroke(Color.WHITE);
                    vertical.setStrokeWidth(2);
                    
                    Line horizontal = new Line(cx - 6, cy - 2, cx + 6, cy - 2);
                    horizontal.setStroke(Color.WHITE);
                    horizontal.setStrokeWidth(2);

                    gamePane.getChildren().addAll(leftShield, rightShield, splitLine, vertical, horizontal);
                }
                case 'Z' -> { 
                    double outerRadius = r + 2;
                    Rectangle topBtn = new Rectangle(cx - 4, cy - outerRadius - 7, 8, 6);
                    topBtn.setFill(Color.MEDIUMPURPLE);
                    topBtn.setStroke(Color.BLACK);
                    topBtn.setArcWidth(4);
                    topBtn.setArcHeight(4);
                    Rectangle leftBtn = new Rectangle(cx - outerRadius - 3, cy - 10, 7, 5);
                    leftBtn.setFill(Color.MEDIUMPURPLE);
                    leftBtn.setStroke(Color.BLACK);
                    leftBtn.setRotate(-40);
                    Rectangle rightBtn = new Rectangle(cx + outerRadius - 4, cy - 10, 7, 5);
                    rightBtn.setFill(Color.MEDIUMPURPLE);
                    rightBtn.setStroke(Color.BLACK);
                    rightBtn.setRotate(40);
                    Circle ring = new Circle(cx, cy, outerRadius);
                    ring.setFill(Color.TRANSPARENT);
                    ring.setStroke(Color.WHITE);
                    ring.setStrokeWidth(3);
                    Arc slice = new Arc(cx, cy, r - 2, r - 2, 90, -270);
                    slice.setType(ArcType.ROUND);
                    slice.setFill(Color.WHITE);
                    Line hand = new Line(cx, cy, cx + (r * 0.6), cy + (r * 0.6));
                    hand.setStroke(Color.WHITE);
                    hand.setStrokeWidth(2);
                    gamePane.getChildren().addAll(topBtn, leftBtn, rightBtn, ring, slice, hand);
                }
                default -> {
                    Circle disc = new Circle(cx, cy, r, Color.MEDIUMPURPLE);
                    disc.setStroke(Color.BLACK);
                    Text letter = new Text(cx - 6, cy + 7, String.valueOf(item.symbol()));
                    letter.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    gamePane.getChildren().addAll(disc, letter);
                }
            }
        }
    }

    private void drawEnemies(Game game) {
        for (Enemy e : game.getEnemies()) {
            
            Image img = imageFor(e);
            if (img != null) {
                SpriteMotion motion = motions.computeIfAbsent(e, o -> new SpriteMotion(TILE_SIZE));
                motion.update(e.getPosition(), frameSeconds, e.getSpeed()); 

                ImageView view = new ImageView(img);
                view.setFitWidth(TILE_SIZE);
                view.setFitHeight(TILE_SIZE);
                view.setX(motion.x());
                view.setY(motion.y());
                gamePane.getChildren().add(view);
            }
        }
    }

    private Image imageFor(Enemy e) {
        if (e instanceof Chaser) return chaserSprite;
        if (e instanceof Bomber) return bomberSprite;
        if (e instanceof Leech)  return leechSprite;
        return null;
    }

    private VBox buildGameOverOverlay(Game game) {
        double width = game.getMaze().getWidth() * TILE_SIZE;
        double height = game.getMaze().getHeight() * TILE_SIZE;

        Text deadText = new Text("GAME OVER");
        deadText.setFill(Color.RED);
        deadText.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        Button restartBtn = new Button("Restart Level");
        restartBtn.setStyle("-fx-font-size: 18px; -fx-padding: 10px;");
        restartBtn.setOnAction(e -> {
            if (onRestartListener != null) onRestartListener.run();
        });

        VBox vbox = new VBox(20, deadText, restartBtn);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(width, height);
        vbox.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        return vbox;
    }

    private VBox buildVictoryOverlay(Game game) {
        double width = game.getMaze().getWidth() * TILE_SIZE;
        double height = game.getMaze().getHeight() * TILE_SIZE;

        Text winText = new Text("YOU ESCAPED!");
        winText.setFill(Color.LIMEGREEN);
        winText.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        Button againBtn = new Button("Play again");
        againBtn.setStyle("-fx-font-size: 18px; -fx-padding: 10px;");
        againBtn.setOnAction(e -> {
            if (onNewGameListener != null) onNewGameListener.run();
        });

        VBox vbox = new VBox(20, winText, againBtn);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(width, height);
        vbox.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        return vbox;
    }
}