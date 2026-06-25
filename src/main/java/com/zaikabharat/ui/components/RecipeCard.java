package com.zaikabharat.ui.components;

import com.zaikabharat.models.Recipe;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class RecipeCard extends VBox {

    public RecipeCard(Recipe recipe, Runnable onClick) {
        getStyleClass().add("recipe-card");
        setPadding(new Insets(16, 18, 16, 18));
        setSpacing(8);
        setPrefWidth(228);
        setMinWidth(190);
        setCursor(javafx.scene.Cursor.HAND);
        setOnMouseClicked(e -> onClick.run());

        // Name row + veg dot
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.TOP_LEFT);

        Label name = new Label(recipe.getName());
        name.getStyleClass().add("card-name");
        name.setWrapText(true);
        HBox.setHgrow(name, Priority.ALWAYS);

        Circle dot = new Circle(5);
        dot.getStyleClass().add(recipe.isVeg() ? "veg-dot" : "nveg-dot");
        topRow.getChildren().addAll(name, dot);

        // Origin
        Label origin = new Label("📍 " + recipe.getOrigin());
        origin.getStyleClass().add("card-meta");

        // Time + difficulty
        HBox metaRow = new HBox(10);
        Label time = new Label("⏱ " + recipe.getTotalTime());
        time.getStyleClass().add("card-meta");

        Label diff = new Label(recipe.getDifficulty());
        diff.getStyleClass().add("diff-" + recipe.getDifficulty().toLowerCase());
        metaRow.getChildren().addAll(time, diff);

        // Spice indicator
        Label spice = new Label("🌶".repeat(Math.max(1, recipe.getSpiceLevel())));
        spice.setStyle("-fx-font-size: 11px;");

        getChildren().addAll(topRow, origin, metaRow, spice);

        // Hover: subtle scale up
        setOnMouseEntered(e -> scale(this, 1.025));
        setOnMouseExited(e  -> scale(this, 1.0));
    }

    private void scale(VBox node, double to) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
        st.setToX(to);
        st.setToY(to);
        st.play();
    }
}
