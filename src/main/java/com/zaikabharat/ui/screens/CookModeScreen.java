package com.zaikabharat.ui.screens;

import com.zaikabharat.models.Recipe;
import com.zaikabharat.models.Recipe.CookStep;
import com.zaikabharat.ui.Navigator;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.util.Duration;
import java.util.List;

public class CookModeScreen extends VBox {

    private final Recipe recipe;
    private final List<CookStep> steps;
    private int idx = 0;

    // Timer state
    private Timeline timerLine;
    private int totalSec = 0;
    private int remaining = 0;
    private boolean running = false;

    // UI refs
    private Label stepNumLbl, stepTxtLbl, timerLbl, timerStatusLbl;
    private Arc timerArc;
    private ProgressBar cookProgress;
    private Label progressLbl;
    private Button timerBtn, prevBtn, nextBtn;

    public CookModeScreen(Recipe recipe) {
        this.recipe = recipe;
        this.steps  = recipe.getCookSteps();
        getStyleClass().add("cook-screen");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(0);
        getChildren().addAll(topBar(), progressSection(), stepArea(), navBar());
        showStep(0);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private HBox topBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("cook-topbar");
        bar.setPadding(new Insets(20, 32, 14, 32));
        bar.setAlignment(Pos.CENTER_LEFT);

        Button exit = new Button("✕  Exit Cook Mode");
        exit.getStyleClass().add("btn-ghost-sm");
        exit.setOnAction(e -> { stopTimer(); Navigator.goBack(); });

        VBox center = new VBox(3);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(center, Priority.ALWAYS);
        Label recipeName = new Label(recipe.getName());
        recipeName.getStyleClass().add("cook-title");
        stepNumLbl = new Label("Step 1 of " + steps.size());
        stepNumLbl.getStyleClass().add("cook-step-lbl");
        center.getChildren().addAll(recipeName, stepNumLbl);

        // Ingredients tooltip button
        Label ingHint = new Label("📋  " + recipe.getIngredients().size() + " ingredients");
        ingHint.getStyleClass().add("cook-ing-hint");
        Tooltip tip = new Tooltip(String.join("  •  ", recipe.getIngredients()));
        tip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(10,4,30,0.95); " +
                     "-fx-text-fill: #eef; -fx-border-color: rgba(255,153,51,0.4);");
        Tooltip.install(ingHint, tip);

        bar.getChildren().addAll(exit, center, ingHint);
        return bar;
    }

    // ── Progress bar ──────────────────────────────────────────────────────────

    private VBox progressSection() {
        VBox sec = new VBox(5);
        sec.setPadding(new Insets(0, 32, 14, 32));

        cookProgress = new ProgressBar(0);
        cookProgress.setMaxWidth(Double.MAX_VALUE);
        cookProgress.getStyleClass().add("cook-prog");

        progressLbl = new Label("Step 1 of " + steps.size());
        progressLbl.getStyleClass().add("cook-prog-lbl");

        sec.getChildren().addAll(cookProgress, progressLbl);
        return sec;
    }

    // ── Step area: text (left) + timer (right) ────────────────────────────────

    private HBox stepArea() {
        HBox area = new HBox(48);
        area.setPadding(new Insets(14, 60, 14, 60));
        area.setAlignment(Pos.CENTER);
        VBox.setVgrow(area, Priority.ALWAYS);

        // Step text column
        VBox textCol = new VBox(14);
        textCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        stepTxtLbl = new Label();
        stepTxtLbl.getStyleClass().add("cook-step-txt");
        stepTxtLbl.setWrapText(true);
        stepTxtLbl.setMaxWidth(Double.MAX_VALUE);

        textCol.getChildren().add(stepTxtLbl);

        // Timer column
        VBox timerCol = timerWidget();
        timerCol.setMinWidth(200);
        timerCol.setMaxWidth(200);

        area.getChildren().addAll(textCol, timerCol);
        return area;
    }

    private VBox timerWidget() {
        VBox col = new VBox(14);
        col.setAlignment(Pos.CENTER);
        col.getStyleClass().add("timer-widget");
        col.setPadding(new Insets(22));

        // Arc + label stack
        StackPane dial = new StackPane();
        dial.setMinSize(148, 148);
        dial.setMaxSize(148, 148);

        Circle bgCircle = new Circle(74);
        bgCircle.getStyleClass().add("timer-dial-bg");

        timerArc = new Arc(74, 74, 64, 64, 90, 360);
        timerArc.setType(ArcType.OPEN);
        timerArc.setFill(null);
        timerArc.getStyleClass().add("timer-arc");

        timerLbl = new Label("--:--");
        timerLbl.getStyleClass().add("timer-lbl");

        dial.getChildren().addAll(bgCircle, timerArc, timerLbl);

        timerStatusLbl = new Label("No timer");
        timerStatusLbl.getStyleClass().add("timer-status");

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER);

        timerBtn = new Button("▶  Start");
        timerBtn.getStyleClass().add("btn-saffron");
        timerBtn.setOnAction(e -> toggleTimer());

        Button resetBtn = new Button("↺");
        resetBtn.getStyleClass().add("btn-ghost-sm");
        resetBtn.setOnAction(e -> resetTimer());

        controls.getChildren().addAll(timerBtn, resetBtn);
        col.getChildren().addAll(dial, timerStatusLbl, controls);
        return col;
    }

    // ── Navigation bar ────────────────────────────────────────────────────────

    private HBox navBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("cook-nav");
        bar.setPadding(new Insets(14, 32, 24, 32));
        bar.setAlignment(Pos.CENTER);

        prevBtn = new Button("← Previous Step");
        prevBtn.getStyleClass().add("btn-ghost");
        prevBtn.setOnAction(e -> prevStep());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        nextBtn = new Button("Next Step →");
        nextBtn.getStyleClass().add("btn-saffron");
        nextBtn.setOnAction(e -> nextStep());

        bar.getChildren().addAll(prevBtn, sp, nextBtn);
        return bar;
    }

    // ── Step logic ────────────────────────────────────────────────────────────

    private void showStep(int i) {
        stopTimer();
        if (steps.isEmpty()) {
            stepTxtLbl.setText("No step-by-step guide available for this recipe.");
            stepNumLbl.setText("—");
            prevBtn.setDisable(true);
            return;
        }
        idx = i;
        CookStep step = steps.get(i);

        // Fade-in animation on text
        stepTxtLbl.setOpacity(0.1);
        FadeTransition ft = new FadeTransition(Duration.millis(200), stepTxtLbl);
        ft.setToValue(1.0);
        ft.play();

        stepTxtLbl.setText(step.text());
        stepNumLbl.setText("Step " + (i + 1) + " of " + steps.size());
        progressLbl.setText("Step " + (i + 1) + " of " + steps.size());
        cookProgress.setProgress((double)(i + 1) / steps.size());

        prevBtn.setDisable(i == 0);
        nextBtn.setText(i == steps.size() - 1 ? "✓  Done!" : "Next Step →");
        nextBtn.getStyleClass().removeAll("btn-green");
        if (i == steps.size() - 1) nextBtn.getStyleClass().add("btn-green");

        // Timer setup
        if (step.hasTimer()) {
            totalSec     = step.timerSeconds();
            remaining    = totalSec;
            timerBtn.setDisable(false);
            timerBtn.setText("▶  Start");
            timerStatusLbl.setText(fmt(totalSec) + " for this step");
        } else {
            totalSec  = 0;
            remaining = 0;
            timerLbl.setText("--:--");
            timerBtn.setDisable(true);
            timerBtn.setText("No Timer");
            timerStatusLbl.setText("No timer for this step");
            timerArc.setLength(360);
        }
        updateDial();
    }

    private void nextStep() {
        if (idx < steps.size() - 1) showStep(idx + 1);
        else { stopTimer(); Navigator.goBack(); }
    }

    private void prevStep() { if (idx > 0) showStep(idx - 1); }

    // ── Timer logic ───────────────────────────────────────────────────────────

    private void toggleTimer() {
        if (running) pauseTimer(); else startTimer();
    }

    private void startTimer() {
        if (remaining <= 0) remaining = totalSec;
        running = true;
        timerBtn.setText("⏸  Pause");

        timerLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining--;
            updateDial();
            if (remaining <= 0) onTimerDone();
        }));
        timerLine.setCycleCount(remaining);
        timerLine.play();
    }

    private void pauseTimer() {
        if (timerLine != null) timerLine.pause();
        running = false;
        timerBtn.setText("▶  Resume");
    }

    private void stopTimer() {
        if (timerLine != null) { timerLine.stop(); timerLine = null; }
        running = false;
    }

    private void resetTimer() {
        stopTimer();
        remaining = totalSec;
        timerBtn.setText("▶  Start");
        if (totalSec > 0) timerBtn.setDisable(false);
        updateDial();
        timerArc.setLength(360);
    }

    private void onTimerDone() {
        stopTimer();
        timerLbl.setText("Done!");
        timerBtn.setText("✓  Done");
        // Pulse green flash
        Timeline flash = new Timeline(
            new KeyFrame(Duration.ZERO,          ev -> timerLbl.setStyle("-fx-text-fill:#4ECC6A;")),
            new KeyFrame(Duration.millis(350),   ev -> timerLbl.setStyle("-fx-text-fill:#F5F5F5;")),
            new KeyFrame(Duration.millis(700),   ev -> timerLbl.setStyle("-fx-text-fill:#4ECC6A;")),
            new KeyFrame(Duration.millis(1050),  ev -> timerLbl.setStyle("-fx-text-fill:#F5F5F5;"))
        );
        flash.play();
    }

    private void updateDial() {
        timerLbl.setText(fmt(remaining));
        if (totalSec > 0)
            timerArc.setLength(360.0 * remaining / totalSec);
    }

    private String fmt(int sec) {
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
}
