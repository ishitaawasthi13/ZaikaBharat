package com.zaikabharat.ui.screens;

import com.zaikabharat.models.Recipe;
import com.zaikabharat.services.RecipeService;
import com.zaikabharat.ui.Navigator;
import com.zaikabharat.ui.components.NutritionPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class RecipeDetailScreen extends ScrollPane {

    private final Recipe recipe;
    private final RecipeService service;

    public RecipeDetailScreen(Recipe recipe, RecipeService service) {
        this.recipe  = recipe;
        this.service = service;
        getStyleClass().addAll("screen", "edge-scroll");
        setFitToWidth(true);

        VBox page = new VBox(0);
        page.getChildren().addAll(
            header(),
            infoRow(),
            mainColumns(),
            actionBar()
        );
        setContent(page);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private VBox header() {
        VBox h = new VBox(10);
        h.getStyleClass().add("detail-header");
        h.setPadding(new Insets(24, 32, 18, 32));

        Button back = new Button("← Back");
        back.getStyleClass().add("btn-ghost-sm");
        back.setOnAction(e -> Navigator.goBack());

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(recipe.getName());
        name.getStyleClass().add("detail-title");
        name.setWrapText(true);
        HBox.setHgrow(name, Priority.ALWAYS);

        Circle dot = new Circle(7);
        dot.getStyleClass().add(recipe.isVeg() ? "veg-dot" : "nveg-dot");
        Label vegLbl = new Label(recipe.isVeg() ? "VEG" : "NON-VEG");
        vegLbl.getStyleClass().add(recipe.isVeg() ? "veg-lbl" : "nveg-lbl");
        HBox vegRow = new HBox(6, dot, vegLbl);
        vegRow.setAlignment(Pos.CENTER);

        titleRow.getChildren().addAll(name, vegRow);

        Label info = new Label(recipe.getInfo());
        info.getStyleClass().add("detail-info");
        info.setWrapText(true);

        h.getChildren().addAll(back, titleRow, info);
        return h;
    }

    // ── Info chips row ────────────────────────────────────────────────────────

    private HBox infoRow() {
        HBox row = new HBox(12);
        row.setPadding(new Insets(0, 32, 20, 32));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            chip("📍", recipe.getOrigin()),
            chip("⏱", recipe.getTotalTime()),
            chip("📊", recipe.getDifficulty()),
            chip("🌶".repeat(Math.max(1, recipe.getSpiceLevel())), "Spice"),
            chip("🍽", recipe.getType())
        );
        return row;
    }

    private VBox chip(String icon, String label) {
        VBox c = new VBox(4);
        c.getStyleClass().add("info-chip");
        c.setPadding(new Insets(10, 16, 10, 16));
        c.setAlignment(Pos.CENTER);
        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size: 15px;");
        Label lb = new Label(label);
        lb.getStyleClass().add("chip-text");
        lb.setWrapText(true);
        lb.setMaxWidth(120);
        lb.setAlignment(Pos.CENTER);
        c.getChildren().addAll(ic, lb);
        return c;
    }

    // ── Three columns: ingredients | steps | nutrition ─────────────────────────

    private HBox mainColumns() {
        HBox cols = new HBox(14);
        cols.setPadding(new Insets(0, 32, 18, 32));

        VBox ing  = ingredientsPanel();
        VBox stps = stepsPanel();
        NutritionPanel nut = new NutritionPanel(recipe);

        HBox.setHgrow(ing,  Priority.ALWAYS);
        HBox.setHgrow(stps, Priority.ALWAYS);
        HBox.setHgrow(nut,  Priority.ALWAYS);

        cols.getChildren().addAll(ing, stps, nut);
        return cols;
    }

    private VBox ingredientsPanel() {
        VBox p = new VBox(10);
        p.getStyleClass().add("glass-card");
        p.setPadding(new Insets(18));

        Label title = new Label("Ingredients");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(7);
        recipe.getIngredients().forEach(ing -> {
            CheckBox cb = new CheckBox(cap(ing.trim()));
            cb.getStyleClass().add("ing-check");
            list.getChildren().add(cb);
        });

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        sp.setPrefHeight(240);
        VBox.setVgrow(sp, Priority.ALWAYS);

        p.getChildren().addAll(title, sp);
        return p;
    }

    private VBox stepsPanel() {
        VBox p = new VBox(10);
        p.getStyleClass().add("glass-card");
        p.setPadding(new Insets(18));

        Label title = new Label("Method");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(10);
        String[] lines = recipe.getPreparationSteps().split("\\|");
        for (int i = 0; i < lines.length; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);

            Label num = new Label(String.valueOf(i + 1));
            num.getStyleClass().add("step-num");
            num.setMinWidth(24);

            String txt = lines[i].replaceAll("^\\d+\\.\\s*", "").trim();
            Label step = new Label(txt);
            step.getStyleClass().add("step-txt");
            step.setWrapText(true);
            HBox.setHgrow(step, Priority.ALWAYS);

            row.getChildren().addAll(num, step);
            list.getChildren().add(row);
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        sp.setPrefHeight(240);
        VBox.setVgrow(sp, Priority.ALWAYS);

        p.getChildren().addAll(title, sp);
        return p;
    }

    // ── Action bar ────────────────────────────────────────────────────────────

    private HBox actionBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(4, 32, 30, 32));
        bar.setAlignment(Pos.CENTER_LEFT);

        Button cook = new Button("🔥  Start Cooking");
        cook.getStyleClass().add("btn-saffron-lg");
        cook.setOnAction(e -> Navigator.navigateTo(new CookModeScreen(recipe)));

        Label serving = new Label("Best served with: " + recipe.getBestServingWith());
        serving.getStyleClass().add("serving-lbl");
        serving.setWrapText(true);
        HBox.setHgrow(serving, Priority.ALWAYS);

        bar.getChildren().addAll(cook, serving);
        return bar;
    }

    private String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
