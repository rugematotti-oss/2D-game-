// Owner: Totti
package com.mazeescape.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class MainMenu extends StackPane {

    private final ImageView stickmanView;
    private final ProgressIndicator loadingCircle;
    private final VBox buttonBox;
    private int animFrame = 0;

    public MainMenu(Runnable onStartGame, Runnable onSettings, Runnable onLevels) {
        this.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("MAZE ESCAPE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        title.setTextFill(Color.web("#e94560"));
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(233,69,96,0.8), 20, 0, 0, 0);");

        stickmanView = new ImageView();
        stickmanView.setFitWidth(128);
        stickmanView.setFitHeight(128);
        stickmanView.setTranslateY(20);
        
        Timeline runTimeline = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            animFrame = (animFrame + 1) % 8;
            stickmanView.setImage(SpriteFactory.getPlayerSprite(0, animFrame));
        }));
        runTimeline.setCycleCount(Timeline.INDEFINITE);
        runTimeline.play();

        // Renamed buttons for better UX
        Button startBtn = createMenuButton("START GAME");
        Button settingsBtn = createMenuButton("HOW TO PLAY");
        Button levelsBtn = createMenuButton("LEVELS");

        buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startBtn, settingsBtn, levelsBtn);

        loadingCircle = new ProgressIndicator();
        loadingCircle.setStyle("-fx-progress-color: #e94560;");
        loadingCircle.setPrefSize(80, 80);
        loadingCircle.setVisible(false);

        VBox layout = new VBox(40);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, stickmanView, buttonBox, loadingCircle);
        this.getChildren().add(layout);

        // --- Actions ---
        startBtn.setOnAction(e -> {
            buttonBox.setVisible(false);
            stickmanView.setVisible(false);
            loadingCircle.setVisible(true);
            runTimeline.stop();

            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(800), this);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(finish -> {
                    this.setVisible(false);
                    onStartGame.run();
                });
                fadeOut.play();
            });
            pause.play();
        });

        settingsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("How to Play");
            alert.setHeaderText("Maze Escape - Controls");
            alert.setContentText("Move: Z/Q/S/D or Arrow Keys\n\n" +
                                 "Use Taser: T\n" +
                                 "Use TimeStop: F\n\n" +
                                 "Find the Key (K), open the Door (D), and reach the Exit (E).\n" +
                                 "Avoid the Chaser (C), Bomber (B), and Leech (L)!");
            alert.showAndWait();
        });

        levelsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Levels");
            alert.setHeaderText("Level Progression");
            alert.setContentText("There are 3 levels in Maze Escape.\n\n" +
                                 "Level 1: The Beginning\n" +
                                 "Level 2: The Bomber's Den\n" +
                                 "Level 3: The Final Chase\n\n" +
                                 "Find the key to unlock the door to the next level!");
            alert.showAndWait();
        });
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setTextFill(Color.WHITE);
        
        String defaultStyle = "-fx-background-color: #16213e; -fx-border-color: #0f3460; -fx-border-width: 2px; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #e94560; -fx-border-color: #e94560; -fx-border-width: 2px; -fx-cursor: hand;";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        
        return btn;
    }
}