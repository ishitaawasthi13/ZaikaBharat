package com.zaikabharat.services;

import com.zaikabharat.db.DatabaseManager;
import com.zaikabharat.models.Recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MealPlanService {

    private final RecipeService recipeService;

    public MealPlanService(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Gets the current meal plan. Key format: "Day_Meal" (e.g., "Monday_Breakfast").
     */
    public Map<String, Recipe> getMealPlan() {
        Map<String, Recipe> plan = new HashMap<>();
        String sql = "SELECT day_of_week, meal_type, recipe_id FROM meal_plan";
        
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                String day = rs.getString("day_of_week");
                String meal = rs.getString("meal_type");
                int recipeId = rs.getInt("recipe_id");
                
                Recipe r = recipeService.getAll().stream()
                        .filter(recipe -> recipe.getId() == recipeId)
                        .findFirst()
                        .orElse(null);
                        
                if (r != null) {
                    plan.put(day + "_" + meal, r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load meal plan: " + e.getMessage());
        }
        return plan;
    }

    public void saveMeal(String day, String meal, int recipeId) {
        String sql = """
            INSERT INTO meal_plan (day_of_week, meal_type, recipe_id)
            VALUES (?, ?, ?)
            ON CONFLICT(day_of_week, meal_type) DO UPDATE SET recipe_id = excluded.recipe_id
        """;
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, meal);
            ps.setInt(3, recipeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save meal: " + e.getMessage());
        }
    }

    public void clearMeal(String day, String meal) {
        String sql = "DELETE FROM meal_plan WHERE day_of_week = ? AND meal_type = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, meal);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to clear meal: " + e.getMessage());
        }
    }

    public void clearAll() {
        String sql = "DELETE FROM meal_plan";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to clear all meals: " + e.getMessage());
        }
    }
}
