package com.zaikabharat.ui.screens;

import com.zaikabharat.models.Recipe;
import com.zaikabharat.services.RecipeService;
import com.zaikabharat.ui.Navigator;
import com.zaikabharat.ui.components.RecipeCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class HomeScreen extends VBox {

    private final RecipeService service;
    private FlowPane grid;
    private boolean vegOnly = false;
    private String activeTag = "all";

    public HomeScreen(RecipeService service) {
        this.service = service;
        getStyleClass().add("screen");
        getChildren().addAll(header(), filterRow(), gridScroll());
        refresh();
    }

    // ── UI Sections ──────────────────────────────────────────────────────────

    private HBox header() {
        HBox h = new HBox();
        h.getStyleClass().add("screen-header");
        h.setPadding(new Insets(28, 32, 16, 32));
        h.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label t = new Label("Browse Recipes");
        t.getStyleClass().add("screen-title");
        Label s = new Label("Explore " + service.getAll().size() + " authentic Indian dishes");
        s.getStyleClass().add("screen-subtitle");
        titles.getChildren().addAll(t, s);
        HBox.setHgrow(titles, Priority.ALWAYS);

        ToggleButton veg = new ToggleButton("🌿  Veg Only");
        veg.getStyleClass().add("veg-toggle");
        veg.setOnAction(e -> { vegOnly = veg.isSelected(); refresh(); });

        h.getChildren().addAll(titles, veg);
        return h;
    }

    private ScrollPane filterRow() {
        HBox pills = new HBox(8);
        pills.setPadding(new Insets(0, 32, 14, 32));
        pills.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        String[][] cats = {
            {"All","all"}, {"North India","north_india"}, {"South India","south_india"},
            {"East India","east_india"}, {"West India","west_india"}, {"Central India","central_india"},
            {"Festival","festival"}, {"Spicy","spicy"}, {"Sweet","sweet"},
            {"Hosteller","hosteller"}, {"Sickness Care","sickness"}
        };

        for (String[] c : cats) {
            ToggleButton btn = new ToggleButton(c[0]);
            btn.getStyleClass().add("filter-pill");
            btn.setToggleGroup(group);
            btn.setUserData(c[1]);
            btn.setOnAction(e -> { if (btn.isSelected()) { activeTag = (String) btn.getUserData(); refresh(); }});
            pills.getChildren().add(btn);
        }
        ((ToggleButton) pills.getChildren().get(0)).setSelected(true);

        ScrollPane sp = new ScrollPane(pills);
        sp.setFitToHeight(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("edge-scroll");
        return sp;
    }

    private ScrollPane gridScroll() {
        grid = new FlowPane(14, 14);
        grid.setPadding(new Insets(8, 32, 32, 32));

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private void refresh() {
        List<Recipe> list = service.getAll().stream()
            .filter(r -> activeTag.equals("all")
                || r.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(activeTag)))
            .filter(r -> !vegOnly || r.isVeg())
            .toList();

        grid.getChildren().clear();
        for (Recipe r : list) {
            grid.getChildren().add(new RecipeCard(r, () ->
                Navigator.navigateTo(new RecipeDetailScreen(r, service))));
        }
        if (list.isEmpty()) {
            Label empty = new Label("No recipes found for this filter.");
            empty.getStyleClass().add("placeholder");
            grid.getChildren().add(empty);
        }
    }
}
