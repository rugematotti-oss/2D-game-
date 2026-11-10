// Owner: Totti & Naim
package com.mazeescape;

import com.mazeescape.core.Direction;
import com.mazeescape.core.Game;
import com.mazeescape.entities.items.Taser;
import com.mazeescape.entities.items.TimeStop;
import com.mazeescape.maze.LevelManager;
import com.mazeescape.save.SaveException;
import com.mazeescape.save.SaveManager;
import com.mazeescape.ui.JavaFxRenderer;
import com.mazeescape.ui.MainMenu;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    private static final int TILE_SIZE = 48;

    private final LevelManager levels = new LevelManager();
    private final SaveManager saves = new SaveManager();

    private Game game;
    private JavaFxRenderer renderer;
    
    private StackPane rootPane;
    private Pane gameLayer;
    private Pane overlayLayer;
    private MainMenu menuLayer;
    private Scene scene;
    
    private long lastNano = 0;
    /** True once the current level's exit has been handled, so it is handled only once. */
    private boolean completionHandled;

    @Override
    public void start(Stage stage) {
        gameLayer = new Pane();
        overlayLayer = new Pane();
        
        renderer = new JavaFxRenderer(gameLayer, overlayLayer);
        
        renderer.setOnRestartListener(() -> {
            show(levels.restart());
            autosave();
        });
        
        renderer.setOnNewGameListener(() -> {
            show(levels.newGame());
            autosave();
        });

        menuLayer = new MainMenu(this::startGame, this::showSettings, this::showLevels);

        rootPane = new StackPane(gameLayer, overlayLayer, menuLayer);
        
        scene = new Scene(rootPane);
        
        // NEW: Handle both key press and release
        scene.setOnKeyPressed(event -> handleInputPressed(event.getCode()));
        scene.setOnKeyReleased(event -> handleInputReleased(event.getCode()));

        stage.setTitle("Maze Escape");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNano == 0) { lastNano = now; return; }
                double dt = (now - lastNano) / 1_000_000_000.0;
                lastNano = now;
                
                if (game != null && !menuLayer.isVisible()) {
                    game.updateRealTime(Math.min(dt, 0.05));
                    // Moves now happen inside updateRealTime, so the exit is detected here.
                    if (game.isLevelComplete() && !completionHandled) {
                        completionHandled = true;
                        onLevelComplete();
                    }
                    renderer.render(game);
                }
            }
        };
        loop.start();
    }

    private void startGame() {
        Game fresh = levels.newGame();
        show(fresh);
        autosave();
    }

    private void showSettings() { }
    private void showLevels() { }

    private void autosave() {
        try {
            saves.save(levels.toSaveData());
        } catch (SaveException e) {
            System.err.println("Could not save: " + e.getMessage());
        }
    }

    private void show(Game newGame) {
        game = newGame;
        completionHandled = false;

        double width = game.getMaze().getWidth() * TILE_SIZE;
        double height = game.getMaze().getHeight() * TILE_SIZE;

        rootPane.setPrefSize(width, height);
        rootPane.setMinSize(width, height);
        rootPane.setMaxSize(width, height);
        
        gameLayer.setPrefSize(width, height);
        overlayLayer.setPrefSize(width, height);
        menuLayer.setPrefSize(width, height);

        scene.getWindow().setWidth(width);
        scene.getWindow().setHeight(height);

        renderer.reset(); 
        gameLayer.requestFocus();
    }

    /** Called when a key is pressed. */
    private void handleInputPressed(KeyCode code) {
        if (game == null || !game.getPlayer().isAlive() || game.isLevelComplete()) return;
        if (menuLayer.isVisible()) return;

        try {
            switch (code) {
                case T -> game.getPlayer().use(Taser.class, game);
                case F -> game.getPlayer().use(TimeStop.class, game);
                default -> {
                    Direction d = switch (code) {
                        case Z, UP -> Direction.UP;
                        case S, DOWN -> Direction.DOWN;
                        case Q, LEFT -> Direction.LEFT;
                        case D, RIGHT -> Direction.RIGHT;
                        default -> null;
                    };
                    if (d != null) {
                        // Tell the player to start moving in this direction
                        game.setPlayerDirection(d);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Game loop error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Called when a key is released. */
    private void handleInputReleased(KeyCode code) {
        if (game == null || !game.getPlayer().isAlive() || game.isLevelComplete()) return;
        if (menuLayer.isVisible()) return;

        // If a movement key was released, tell the player to stop moving
        if (code == KeyCode.Z || code == KeyCode.UP ||
            code == KeyCode.S || code == KeyCode.DOWN ||
            code == KeyCode.Q || code == KeyCode.LEFT ||
            code == KeyCode.D || code == KeyCode.RIGHT) {
            game.stopPlayerMovement();
        }
    }

    private void onLevelComplete() {
        if (levels.hasNextLevel()) {
            show(levels.nextLevel());
            autosave();
        } else {
            try {
                saves.delete();
            } catch (SaveException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}