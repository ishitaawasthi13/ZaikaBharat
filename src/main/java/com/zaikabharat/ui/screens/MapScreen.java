package com.zaikabharat.ui.screens;

import com.zaikabharat.models.Recipe;
import com.zaikabharat.services.RecipeService;
import com.zaikabharat.ui.Navigator;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import netscape.javascript.JSObject;
import java.net.URL;
import java.util.List;

public class MapScreen extends StackPane {

    private final RecipeService service;
    private Label regionTitle;
    private Label regionDesc;
    private Label recipeCountLabel;
    private VBox recipeList;
    private final MapBridge bridge = new MapBridge();

    public MapScreen(RecipeService service) {
        this.service = service;
        getStyleClass().add("screen");
        
        VBox mapSide = buildMapSide();
        VBox infoSide = buildInfoSide();

        // Make info side a floating panel in the top right
        infoSide.setMaxSize(280, 400);
        infoSide.setPickOnBounds(false); // Let clicks pass through empty space in the StackPane
        StackPane.setAlignment(infoSide, Pos.TOP_RIGHT);
        StackPane.setMargin(infoSide, new Insets(20));

        getChildren().addAll(mapSide, infoSide);
    }

    // ── Left: WebView map ─────────────────────────────────────────────────────

    private VBox buildMapSide() {
        VBox side = new VBox(0);
        side.getStyleClass().add("map-side");

        VBox header = new VBox(4);
        header.setPadding(new Insets(26, 22, 10, 32));
        Label title = new Label("\uD83D\uDDFA  India Map Discovery");
        title.getStyleClass().add("screen-title");
        Label sub = new Label("Click a region to explore its culinary heritage");
        sub.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(title, sub);

        WebView webView = new WebView();
        webView.getStyleClass().add("map-webview");
        VBox.setVgrow(webView, Priority.ALWAYS);

        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.getLoadWorker().stateProperty().addListener((obs, old, nw) -> {
            if (nw == Worker.State.SUCCEEDED) {
                JSObject win = (JSObject) engine.executeScript("window");
                win.setMember("javaApp", bridge);
            }
        });

        URL url = getClass().getResource("/com/zaikabharat/web/india-map.html");
        if (url != null) engine.load(url.toExternalForm());

        side.getChildren().addAll(header, webView);
        return side;
    }

    // ── Right: region info + recipe list ──────────────────────────────────────

    private VBox buildInfoSide() {
        VBox side = new VBox(10);
        side.getStyleClass().add("region-side");
        // Add a floating style
        side.setStyle("-fx-background-color: rgba(20, 20, 20, 0.85); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 4);");
        side.setPadding(new Insets(14));

        // Glass header card
        VBox headerCard = new VBox(4);
        headerCard.getStyleClass().add("region-header");
        headerCard.setStyle("-fx-background-color: transparent;");
        headerCard.setPadding(new Insets(6));

        regionTitle = new Label("Select a Region");
        regionTitle.getStyleClass().add("region-title");
        regionTitle.setStyle("-fx-font-size: 16px;");

        regionDesc = new Label("Click any coloured region on the map\nto discover authentic recipes from that area.");
        regionDesc.getStyleClass().add("region-desc");
        regionDesc.setStyle("-fx-font-size: 12px;");
        regionDesc.setWrapText(true);

        headerCard.getChildren().addAll(regionTitle, regionDesc);

        recipeCountLabel = new Label("");
        recipeCountLabel.getStyleClass().add("region-count");

        recipeList = new VBox(10);
        ScrollPane sp = new ScrollPane(recipeList);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-scroll");
        VBox.setVgrow(sp, Priority.ALWAYS);

        side.getChildren().addAll(headerCard, recipeCountLabel, sp);
        return side;
    }

    // ── Region selection logic ────────────────────────────────────────────────

    void showRegion(String regionId, String regionName, String desc) {
        regionTitle.setText(regionName);
        regionDesc.setText(desc);
        recipeList.getChildren().clear();

        List<Recipe> recipes = service.getByRegion(regionId);
        if (recipes.isEmpty()) {
            recipeCountLabel.setText("");
            Label none = new Label("No recipes found for " + regionName);
            none.getStyleClass().add("placeholder");
            recipeList.getChildren().add(none);
            return;
        }

        recipeCountLabel.setText(recipes.size() + " recipes from " + regionName);

        for (Recipe r : recipes)
            recipeList.getChildren().add(miniCard(r));
    }

    private VBox miniCard(Recipe recipe) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-card");
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e ->
            Navigator.navigateTo(new RecipeDetailScreen(recipe, service)));

        // Top row: name + veg/nveg indicator
        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(recipe.getName());
        name.getStyleClass().add("mini-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label icon = new Label(recipe.isVeg() ? "\uD83C\uDF3F" : "\uD83C\uDF57");
        icon.setStyle("-fx-font-size: 13px;");

        top.getChildren().addAll(name, icon);

        // Meta row: origin + time + difficulty
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(recipe.getOrigin() + "  •  " + recipe.getTotalTime());
        meta.getStyleClass().add("mini-meta");
        meta.setStyle("-fx-font-size: 11px;");
        HBox.setHgrow(meta, Priority.ALWAYS);

        Label diff = new Label(recipe.getDifficulty());
        diff.getStyleClass().add("diff-" + recipe.getDifficulty().toLowerCase());
        diff.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");

        metaRow.getChildren().addAll(meta, diff);

        card.getChildren().addAll(top, metaRow);
        return card;
    }

    // ── JS → Java bridge ─────────────────────────────────────────────────────

    public class MapBridge {
        /** Called by the SVG map JavaScript on region click. */
        public void onRegionClick(String regionId, String regionName, String desc) {
            Platform.runLater(() -> showRegion(regionId, regionName, desc));
        }
    }
}
