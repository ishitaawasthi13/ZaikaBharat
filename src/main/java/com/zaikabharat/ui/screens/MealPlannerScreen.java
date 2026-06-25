package com.zaikabharat.ui.screens;

import com.zaikabharat.models.Recipe;
import com.zaikabharat.services.MealPlanService;
import com.zaikabharat.services.PdfExportService;
import com.zaikabharat.services.RecipeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MealPlannerScreen extends BorderPane {

    private final RecipeService recipeService;
    private final MealPlanService mealPlanService;
    private final PdfExportService pdfExportService = new PdfExportService();
    private static final DataFormat RECIPE_FORMAT = new DataFormat("application/x-recipe-id");

    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private final String[] meals = {"Breakfast", "Lunch", "Dinner"};

    private GridPane planGrid;

    public MealPlannerScreen(RecipeService recipeService) {
        this.recipeService = recipeService;
        this.mealPlanService = new MealPlanService(recipeService);

        getStyleClass().add("screen");
        setPadding(new Insets(20));

        setLeft(buildRecipeSidebar());
        setCenter(buildMainArea());
    }

    private VBox buildRecipeSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(0, 20, 0, 0));

        Label title = new Label("Recipes");
        title.getStyleClass().add("screen-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search recipes to drag...");

        VBox recipeList = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(recipeList);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.getStyleClass().add("edge-scroll");

        Runnable updateList = () -> {
            recipeList.getChildren().clear();
            String q = searchField.getText().toLowerCase();
            List<Recipe> filtered = recipeService.getAll().stream()
                    .filter(r -> r.getName().toLowerCase().contains(q))
                    .collect(Collectors.toList());

            for (Recipe r : filtered) {
                recipeList.getChildren().add(createDraggableRecipeCard(r));
            }
        };

        searchField.textProperty().addListener((obs, old, nw) -> updateList.run());
        updateList.run();

        sidebar.getChildren().addAll(title, searchField, scrollPane);
        return sidebar;
    }

    private VBox createDraggableRecipeCard(Recipe recipe) {
        VBox card = new VBox(4);
        card.getStyleClass().add("mini-card");
        card.setPadding(new Insets(10));
        card.setCursor(javafx.scene.Cursor.OPEN_HAND);

        Label name = new Label(recipe.getName());
        name.setStyle("-fx-font-weight: bold;");
        Label meta = new Label(recipe.getDifficulty() + " • " + recipe.getTotalTime());
        meta.setStyle("-fx-font-size: 11px;");

        card.getChildren().addAll(name, meta);

        // Setup Drag and Drop
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.put(RECIPE_FORMAT, String.valueOf(recipe.getId()));
            content.putString(recipe.getName());
            db.setContent(content);
            event.consume();
        });

        return card;
    }

    private VBox buildMainArea() {
        VBox main = new VBox(20);
        main.getStyleClass().add("content-area");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Weekly Meal Planner");
        title.getStyleClass().add("screen-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("Export Shopping List (PDF)");
        exportBtn.getStyleClass().addAll("btn", "btn-primary");
        exportBtn.setOnAction(e -> exportPdf());

        header.getChildren().addAll(title, spacer, exportBtn);

        planGrid = new GridPane();
        planGrid.setHgap(10);
        planGrid.setVgap(10);
        VBox.setVgrow(planGrid, Priority.ALWAYS);

        // Add headers
        for (int i = 0; i < days.length; i++) {
            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            planGrid.add(dayLbl, i + 1, 0);
            
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            planGrid.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < meals.length; i++) {
            Label mealLbl = new Label(meals[i]);
            mealLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            planGrid.add(mealLbl, 0, i + 1);
        }

        // Initialize cells
        Map<String, Recipe> currentPlan = mealPlanService.getMealPlan();

        for (int row = 0; row < meals.length; row++) {
            for (int col = 0; col < days.length; col++) {
                String day = days[col];
                String meal = meals[row];
                
                VBox cell = createDropCell(day, meal, currentPlan.get(day + "_" + meal));
                planGrid.add(cell, col + 1, row + 1);
                GridPane.setVgrow(cell, Priority.ALWAYS);
            }
        }

        main.getChildren().addAll(header, planGrid);
        return main;
    }

    private VBox createDropCell(String day, String meal, Recipe existingRecipe) {
        VBox cell = new VBox(5);
        cell.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");
        cell.setPadding(new Insets(10));
        cell.setAlignment(Pos.CENTER);
        cell.setMinHeight(80);

        if (existingRecipe != null) {
            setCellRecipe(cell, day, meal, existingRecipe);
        } else {
            setCellEmpty(cell, day, meal);
        }

        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasContent(RECIPE_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        cell.setOnDragEntered(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasContent(RECIPE_FORMAT)) {
                cell.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-border-color: #2196F3; -fx-border-radius: 8;");
            }
            event.consume();
        });

        cell.setOnDragExited(event -> {
            cell.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(RECIPE_FORMAT)) {
                int recipeId = Integer.parseInt((String) db.getContent(RECIPE_FORMAT));
                Recipe r = recipeService.getAll().stream()
                        .filter(recipe -> recipe.getId() == recipeId)
                        .findFirst().orElse(null);
                if (r != null) {
                    mealPlanService.saveMeal(day, meal, recipeId);
                    setCellRecipe(cell, day, meal, r);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return cell;
    }

    private void setCellEmpty(VBox cell, String day, String meal) {
        cell.getChildren().clear();
        Label placeholder = new Label("Drop Here");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        cell.getChildren().add(placeholder);
    }

    private void setCellRecipe(VBox cell, String day, String meal, Recipe recipe) {
        cell.getChildren().clear();
        Label name = new Label(recipe.getName());
        name.setWrapText(true);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        name.setAlignment(Pos.CENTER);
        
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5252; -fx-padding: 0;");
        removeBtn.setCursor(javafx.scene.Cursor.HAND);
        removeBtn.setOnAction(e -> {
            mealPlanService.clearMeal(day, meal);
            setCellEmpty(cell, day, meal);
        });
        
        HBox top = new HBox(removeBtn);
        top.setAlignment(Pos.TOP_RIGHT);
        
        cell.getChildren().addAll(top, name);
    }

    private void exportPdf() {
        Map<String, Recipe> plan = mealPlanService.getMealPlan();
        if (plan.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Empty Plan");
            alert.setHeaderText(null);
            alert.setContentText("Your meal plan is empty. Drag some recipes first!");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shopping List");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("ZaikaBharat_Shopping_List.pdf");
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            pdfExportService.exportShoppingList(file, plan.values());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Shopping list has been saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        }
    }
}
