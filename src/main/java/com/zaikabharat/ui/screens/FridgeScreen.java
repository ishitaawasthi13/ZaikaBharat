package com.zaikabharat.ui.screens;

import com.zaikabharat.services.RecipeService;
import com.zaikabharat.services.RecipeService.FridgeMatch;
import com.zaikabharat.ui.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.stream.Collectors;

public class FridgeScreen extends BorderPane {

    private final RecipeService service;
    private final Set<String> selected = new HashSet<>();
    private FlowPane chipPane;
    private VBox resultsCol;
    private Label selCount;

    public FridgeScreen(RecipeService service) {
        this.service = service;
        getStyleClass().add("screen");
        setLeft(leftPanel());
        setCenter(rightPanel());
    }

    // ── Left: ingredient chips ────────────────────────────────────────────────

    private VBox leftPanel() {
        VBox p = new VBox(12);
        p.getStyleClass().add("fridge-left");
        p.setPadding(new Insets(28, 14, 28, 28));
        p.setPrefWidth(310);

        Label title = new Label("🧊  My Fridge");
        title.getStyleClass().add("screen-title");
        Label sub = new Label("Tap the ingredients you have at home:");
        sub.getStyleClass().add("screen-subtitle");

        selCount = new Label("0 ingredients selected");
        selCount.getStyleClass().add("sel-count");

        HBox actions = new HBox(8);
        Button find  = new Button("Find Recipes →");
        find.getStyleClass().add("btn-saffron");
        find.setOnAction(e -> findRecipes());

        Button clear = new Button("Clear All");
        clear.getStyleClass().add("btn-ghost");
        clear.setOnAction(e -> clearAll());
        actions.getChildren().addAll(find, clear);

        chipPane = new FlowPane(8, 8);
        chipPane.setPadding(new Insets(4, 0, 0, 0));

        service.getAllIngredients().forEach(ing -> {
            ToggleButton chip = new ToggleButton(ing);
            chip.getStyleClass().add("ing-chip");
            chip.setOnAction(e -> {
                if (chip.isSelected()) selected.add(ing); else selected.remove(ing);
                selCount.setText(selected.size() + " ingredient" +
                    (selected.size() != 1 ? "s" : "") + " selected");
            });
            chipPane.getChildren().add(chip);
        });

        ScrollPane scroll = new ScrollPane(chipPane);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("edge-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        p.getChildren().addAll(title, sub, selCount, actions, scroll);
        return p;
    }

    // ── Right: results ────────────────────────────────────────────────────────

    private VBox rightPanel() {
        resultsCol = new VBox(12);
        resultsCol.setPadding(new Insets(28, 28, 28, 12));

        Label title = new Label("Matching Recipes");
        title.getStyleClass().add("screen-title");

        Label hint = new Label("Select ingredients on the left, then tap Find Recipes.");
        hint.getStyleClass().add("placeholder");

        resultsCol.getChildren().addAll(title, hint);
        return resultsCol;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void findRecipes() {
        if (selected.isEmpty()) return;

        List<FridgeMatch> matches = service.matchByIngredients(new ArrayList<>(selected));
        resultsCol.getChildren().clear();

        Label title = new Label("Matching Recipes");
        title.getStyleClass().add("screen-title");
        Label sub = new Label("Found " + matches.size() + " recipe" +
            (matches.size() != 1 ? "s" : "") + " — sorted by best match");
        sub.getStyleClass().add("screen-subtitle");
        resultsCol.getChildren().addAll(title, sub);

        if (matches.isEmpty()) {
            Label none = new Label("No matches found. Try selecting more ingredients.");
            none.getStyleClass().add("placeholder");
            resultsCol.getChildren().add(none);
            return;
        }

        VBox list = new VBox(10);
        for (FridgeMatch m : matches) list.getChildren().add(matchCard(m));

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        VBox.setVgrow(sp, Priority.ALWAYS);
        resultsCol.getChildren().add(sp);
    }

    private VBox matchCard(FridgeMatch m) {
        VBox card = new VBox(8);
        card.getStyleClass().add("match-card");
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e ->
            Navigator.navigateTo(new RecipeDetailScreen(m.recipe(), service)));

        // Name row + badge
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(m.recipe().getName());
        name.getStyleClass().add("match-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label badge = m.canCook()
            ? badgeLbl("✓  Can Cook!", "badge-green")
            : badgeLbl(m.matchCount() + " / " + m.totalCount() + " ingredients", "badge-info");

        top.getChildren().addAll(name, badge);

        // Progress bar
        ProgressBar bar = new ProgressBar(m.matchPercent() / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().addAll("match-bar",
            m.matchPercent() >= 80 ? "bar-high" : m.matchPercent() >= 50 ? "bar-mid" : "bar-low");

        Label pct = new Label(m.matchPercent() + "% match");
        pct.getStyleClass().add("match-pct");

        card.getChildren().addAll(top, bar, pct);

        // Missing ingredients (capped at 5)
        if (!m.canCook() && !m.missingIngredients().isEmpty()) {
            String missing = m.missingIngredients().stream().limit(5)
                              .collect(Collectors.joining(", "));
            if (m.missingIngredients().size() > 5) missing += "…";
            Label ml = new Label("Missing: " + missing);
            ml.getStyleClass().add("missing-lbl");
            ml.setWrapText(true);
            card.getChildren().add(ml);
        }
        return card;
    }

    private Label badgeLbl(String text, String style) {
        Label l = new Label(text);
        l.getStyleClass().addAll("badge", style);
        return l;
    }

    private void clearAll() {
        selected.clear();
        chipPane.getChildren().forEach(n -> { if (n instanceof ToggleButton tb) tb.setSelected(false); });
        selCount.setText("0 ingredients selected");
    }
}
