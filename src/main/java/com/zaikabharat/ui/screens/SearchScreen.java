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

public class SearchScreen extends BorderPane {

    private final RecipeService service;
    private TextField searchField;
    private CheckBox vegCheck;
    private FlowPane resultsPane;
    private Label countLabel;

    private String region = "all";
    private String cookTime = "any";
    private int spice = 0;

    public SearchScreen(RecipeService service) {
        this.service = service;
        getStyleClass().add("screen");
        setLeft(filterPanel());
        setCenter(mainArea());
        runSearch();
    }

    // ── Filter Panel ─────────────────────────────────────────────────────────

    private VBox filterPanel() {
        VBox p = new VBox(14);
        p.getStyleClass().add("filter-panel");
        p.setPadding(new Insets(28, 18, 28, 28));
        p.setPrefWidth(218);

        Label title = new Label("Filters");
        title.getStyleClass().add("filter-title");

        // Veg
        vegCheck = new CheckBox("Vegetarian only");
        vegCheck.getStyleClass().add("filter-check");
        vegCheck.setOnAction(e -> runSearch());

        // Region
        ToggleGroup rg = new ToggleGroup();
        VBox regionBox = new VBox(6, sectionLbl("Region"));
        for (String[] r : new String[][]{{"All","all"},{"North India","north_india"},
                {"South India","south_india"},{"East India","east_india"},
                {"West India","west_india"},{"Central India","central_india"}}) {
            RadioButton rb = radioOf(r[0], r[1], rg);
            if (r[1].equals("all")) rb.setSelected(true);
            regionBox.getChildren().add(rb);
        }
        rg.selectedToggleProperty().addListener((o,a,n) -> {
            if (n != null) { region = (String) ((RadioButton)n).getUserData(); runSearch(); }
        });

        // Cook time
        ToggleGroup tg = new ToggleGroup();
        VBox timeBox = new VBox(6, sectionLbl("Cook Time"));
        for (String[] t : new String[][]{{"Any Time","any"},{"Quick (≤ 15 min)","quick"},
                {"30 Minutes","30min"},{"1 Hour","60min"},{"Long Cook","slow"}}) {
            RadioButton rb = radioOf(t[0], t[1], tg);
            if (t[1].equals("any")) rb.setSelected(true);
            timeBox.getChildren().add(rb);
        }
        tg.selectedToggleProperty().addListener((o,a,n) -> {
            if (n != null) { cookTime = (String) ((RadioButton)n).getUserData(); runSearch(); }
        });

        // Spice
        Label spiceTitle = sectionLbl("Spice Level");
        HBox spiceRow = new HBox(6);
        ToggleGroup sg = new ToggleGroup();
        String[][] spiceOpts = {{"Any","0"},{"🌿","1"},{"🌶","2"},{"🌶🌶","3"},{"🌶🌶🌶","4"}};
        for (String[] s : spiceOpts) {
            ToggleButton tb = new ToggleButton(s[0]);
            tb.getStyleClass().add("spice-pill");
            tb.setUserData(Integer.parseInt(s[1]));
            tb.setToggleGroup(sg);
            if (s[1].equals("0")) tb.setSelected(true);
            spiceRow.getChildren().add(tb);
        }
        sg.selectedToggleProperty().addListener((o,a,n) -> {
            if (n != null) { spice = (int) ((ToggleButton)n).getUserData(); runSearch(); }
        });

        p.getChildren().addAll(title, vegCheck, regionBox, timeBox, spiceTitle, spiceRow);
        return p;
    }

    private Label sectionLbl(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("filter-section");
        VBox.setMargin(l, new Insets(6, 0, 0, 0));
        return l;
    }

    private RadioButton radioOf(String label, String data, ToggleGroup g) {
        RadioButton rb = new RadioButton(label);
        rb.getStyleClass().add("filter-radio");
        rb.setToggleGroup(g);
        rb.setUserData(data);
        return rb;
    }

    // ── Main Content ─────────────────────────────────────────────────────────

    private VBox mainArea() {
        VBox main = new VBox(14);
        main.setPadding(new Insets(28, 28, 28, 8));

        searchField = new TextField();
        searchField.setPromptText("Search recipes, ingredients, regions…");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((o, a, b) -> runSearch());

        countLabel = new Label();
        countLabel.getStyleClass().add("result-count");

        resultsPane = new FlowPane(14, 14);
        ScrollPane sp = new ScrollPane(resultsPane);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        VBox.setVgrow(sp, Priority.ALWAYS);

        main.getChildren().addAll(searchField, countLabel, sp);
        return main;
    }

    // ── Search Logic ─────────────────────────────────────────────────────────

    private void runSearch() {
        List<Recipe> results = service.search(
            searchField != null ? searchField.getText() : "",
            vegCheck != null && vegCheck.isSelected(),
            region, cookTime, spice
        );
        if (countLabel != null)
            countLabel.setText(results.size() + " recipe" + (results.size() != 1 ? "s" : "") + " found");

        if (resultsPane != null) {
            resultsPane.getChildren().clear();
            for (Recipe r : results)
                resultsPane.getChildren().add(new RecipeCard(r, () ->
                    Navigator.navigateTo(new RecipeDetailScreen(r, service))));
            if (results.isEmpty()) {
                Label none = new Label("No recipes match your filters.");
                none.getStyleClass().add("placeholder");
                resultsPane.getChildren().add(none);
            }
        }
    }
}
