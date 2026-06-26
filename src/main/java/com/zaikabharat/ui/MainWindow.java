package com.zaikabharat.ui;

import com.zaikabharat.services.RecipeService;
import com.zaikabharat.ui.screens.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class MainWindow {

    private final BorderPane root = new BorderPane();
    private final RecipeService service = new RecipeService();
    private ToggleGroup navGroup;

    // Lazy screen instances
    private HomeScreen homeScreen;
    private SearchScreen searchScreen;
    private FridgeScreen fridgeScreen;
    private MapScreen mapScreen;
    private MealPlannerScreen mealPlannerScreen;

    public MainWindow() {
        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("content-pane");
        Navigator.init(contentPane);

        root.getStyleClass().add("app-root");
        root.setLeft(buildSidebar());
        root.setCenter(contentPane);
        goHome();
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(228);
        sidebar.setPadding(new Insets(26, 14, 22, 14));

        sidebar.getChildren().addAll(
            buildLogo(),
            divider(),
            buildNav(),
            spacer(),
            buildCredits()
        );
        return sidebar;
    }

    private HBox buildLogo() {
        StackPane badge = new StackPane();
        Circle c = new Circle(22);
        c.getStyleClass().add("logo-bg");
        Label lbl = new Label("ZB");
        lbl.getStyleClass().add("logo-text");
        badge.getChildren().addAll(c, lbl);
        badge.setMinSize(44, 44);
        badge.setMaxSize(44, 44);

        VBox names = new VBox(2);
        Label name = new Label("ZaikaBharat");
        name.getStyleClass().add("app-name");
        Label tag = new Label("Tales Woven in Flavors");
        tag.getStyleClass().add("tagline");
        names.getChildren().addAll(name, tag);

        HBox row = new HBox(12, badge, names);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 6, 0));
        return row;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.getStyleClass().add("sidebar-divider");
        r.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(r, new Insets(2, 0, 10, 0));
        return r;
    }

    private VBox buildNav() {
        navGroup = new ToggleGroup();
        VBox nav = new VBox(2);

        nav.getChildren().addAll(
            navSection("Browse"),
            navBtn("🍽   Browse Recipes", () -> goHome()),
            navBtn("🔍   Smart Search",   () -> goSearch()),
            spacerSmall(),
            navSection("Discover"),
            navBtn("🧊   My Fridge",      () -> goFridge()),
            navBtn("🗺   India Map",       () -> goMap()),
            spacerSmall(),
            navSection("Planning"),
            navBtn("📅   Weekly Planner",  () -> goMealPlanner())
        );

        // Select first by default
        ((ToggleButton) nav.getChildren().get(1)).setSelected(true);
        return nav;
    }

    private Label navSection(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("nav-section");
        l.setPadding(new Insets(10, 8, 3, 8));
        return l;
    }

    private ToggleButton navBtn(String text, Runnable action) {
        ToggleButton btn = new ToggleButton(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-btn");
        btn.setToggleGroup(navGroup);
        btn.setOnAction(e -> { if (btn.isSelected()) action.run(); });
        return btn;
    }

    private VBox buildCredits() {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8, 8, 0, 8));
        Label v = new Label("v1.0  ·  GUI Edition");
        Label c = new Label("Ishita · Kaavya");
        v.getStyleClass().add("sidebar-meta");
        c.getStyleClass().add("sidebar-meta");
        box.getChildren().addAll(v, c);
        return box;
    }

    private Region spacer() {
        Region r = new Region();
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    private Region spacerSmall() {
        Region r = new Region();
        r.setPrefHeight(8);
        return r;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void goHome() {
        if (homeScreen == null) homeScreen = new HomeScreen(service);
        Navigator.navigateTo(homeScreen, false);
        Navigator.clearHistory();
    }

    private void goSearch() {
        if (searchScreen == null) searchScreen = new SearchScreen(service);
        Navigator.navigateTo(searchScreen, false);
        Navigator.clearHistory();
    }

    private void goFridge() {
        if (fridgeScreen == null) fridgeScreen = new FridgeScreen(service);
        Navigator.navigateTo(fridgeScreen, false);
        Navigator.clearHistory();
    }

    private void goMap() {
        if (mapScreen == null) mapScreen = new MapScreen(service);
        Navigator.navigateTo(mapScreen, false);
        Navigator.clearHistory();
    }

    private void goMealPlanner() {
        if (mealPlannerScreen == null) mealPlannerScreen = new MealPlannerScreen(service);
        Navigator.navigateTo(mealPlannerScreen, false);
        Navigator.clearHistory();
    }

    public BorderPane getRoot() { return root; }
}
