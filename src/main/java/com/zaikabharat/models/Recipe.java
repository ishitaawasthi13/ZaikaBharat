package com.zaikabharat.models;

import java.util.*;
import java.util.stream.Collectors;

public class Recipe {

    private final int id;
    private final String name, info, origin, totalTime;
    private final List<String> ingredients, tags;
    private final String preparationSteps, cookStepsRaw, recipe, bestServingWith, type;
    private final boolean isVeg;
    private final int calories, proteinG, carbsG, fiberG, fatG, spiceLevel;
    private final String difficulty;

    public Recipe(int id, String name, String info, String origin, String totalTime,
                  List<String> ingredients, List<String> tags,
                  String preparationSteps, String cookStepsRaw, String recipe,
                  String bestServingWith, String type, boolean isVeg,
                  int calories, int proteinG, int carbsG, int fiberG, int fatG,
                  int spiceLevel, String difficulty) {
        this.id = id;
        this.name = name;
        this.info = info;
        this.origin = origin;
        this.totalTime = totalTime;
        this.ingredients = ingredients;
        this.tags = tags;
        this.preparationSteps = preparationSteps;
        this.cookStepsRaw = cookStepsRaw;
        this.recipe = recipe;
        this.bestServingWith = bestServingWith;
        this.type = type;
        this.isVeg = isVeg;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fiberG = fiberG;
        this.fatG = fatG;
        this.spiceLevel = spiceLevel;
        this.difficulty = difficulty;
    }

    /** Parse cook_steps raw string into structured CookStep list. */
    public List<CookStep> getCookSteps() {
        if (cookStepsRaw == null || cookStepsRaw.isBlank()) return List.of();
        return Arrays.stream(cookStepsRaw.split("\\|"))
            .map(s -> {
                int colon = s.lastIndexOf(':');
                if (colon > 0) {
                    try {
                        return new CookStep(s.substring(0, colon).trim(),
                                            Integer.parseInt(s.substring(colon + 1).trim()));
                    } catch (NumberFormatException ignored) {}
                }
                return new CookStep(s.trim(), 0);
            })
            .collect(Collectors.toList());
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getInfo()          { return info; }
    public String getOrigin()        { return origin; }
    public String getTotalTime()     { return totalTime; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getTags()    { return tags; }
    public String getPreparationSteps() { return preparationSteps; }
    public String getRecipe()        { return recipe; }
    public String getBestServingWith() { return bestServingWith; }
    public String getType()          { return type; }
    public boolean isVeg()           { return isVeg; }
    public int getCalories()         { return calories; }
    public int getProteinG()         { return proteinG; }
    public int getCarbsG()           { return carbsG; }
    public int getFiberG()           { return fiberG; }
    public int getFatG()             { return fatG; }
    public int getSpiceLevel()       { return spiceLevel; }
    public String getDifficulty()    { return difficulty; }

    // ── Inner class ──────────────────────────────────────────────────────────
    public record CookStep(String text, int timerSeconds) {
        public boolean hasTimer() { return timerSeconds > 0; }
    }
}
