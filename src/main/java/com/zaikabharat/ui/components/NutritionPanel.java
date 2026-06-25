package com.zaikabharat.ui.components;

import com.zaikabharat.models.Recipe;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class NutritionPanel extends VBox {

    private final Recipe recipe;
    private int servings = 1;

    private static final String[] LABELS = {"Calories", "Protein", "Carbs", "Fiber", "Fat"};
    private static final String[] UNITS  = {"kcal",    "g",       "g",     "g",    "g"  };
    // Max reference values for the progress bar (100% width)
    private static final int[]    MAXV   = {900, 60, 120, 25, 60};
    // Accent colours per nutrient
    private static final String[] COLORS = {
        "#FF9933", "#4ECC6A", "#64B5F6", "#A5D6A7", "#FFB74D"
    };

    private final Label[]       valueLbls = new Label[5];
    private final ProgressBar[] bars      = new ProgressBar[5];

    public NutritionPanel(Recipe recipe) {
        this.recipe = recipe;
        getStyleClass().add("glass-card");
        setPadding(new Insets(18));
        setSpacing(12);
        buildUI();
        refresh();
    }

    private void buildUI() {
        Label title = new Label("Nutrition Info");
        title.getStyleClass().add("card-title");

        // Serving selector
        HBox servRow = new HBox(10);
        servRow.setAlignment(Pos.CENTER_LEFT);
        Label servLbl = new Label("Servings:");
        servLbl.getStyleClass().add("nutr-label");

        Spinner<Integer> spinner = new Spinner<>(1, 8, 1);
        spinner.setPrefWidth(68);
        spinner.getStyleClass().addAll("serving-spinner");
        spinner.valueProperty().addListener((o, a, n) -> { servings = n; refresh(); });
        servRow.getChildren().addAll(servLbl, spinner);

        Label perServing = new Label("Per serving");
        perServing.getStyleClass().add("nutr-per");

        getChildren().addAll(title, servRow, perServing);

        // Build one row per nutrient
        for (int i = 0; i < LABELS.length; i++) {
            VBox row = new VBox(4);

            HBox labelRow = new HBox();
            Label name = new Label(LABELS[i]);
            name.getStyleClass().add("nutr-label");
            HBox.setHgrow(name, Priority.ALWAYS);

            valueLbls[i] = new Label();
            valueLbls[i].getStyleClass().add("nutr-value");
            labelRow.getChildren().addAll(name, valueLbls[i]);

            bars[i] = new ProgressBar(0);
            bars[i].setMaxWidth(Double.MAX_VALUE);
            bars[i].setPrefHeight(8);
            bars[i].setStyle("-fx-accent: " + COLORS[i] + ";");

            row.getChildren().addAll(labelRow, bars[i]);
            getChildren().add(row);
        }

        Label note = new Label("* Approximate values");
        note.getStyleClass().add("nutr-note");
        getChildren().add(note);
    }

    private void refresh() {
        int[] base = {
            recipe.getCalories(), recipe.getProteinG(),
            recipe.getCarbsG(),   recipe.getFiberG(), recipe.getFatG()
        };
        for (int i = 0; i < LABELS.length; i++) {
            int val = base[i] * servings;
            valueLbls[i].setText(val + " " + UNITS[i]);
            bars[i].setProgress(Math.min(1.0, (double) val / MAXV[i]));
        }
    }
}
