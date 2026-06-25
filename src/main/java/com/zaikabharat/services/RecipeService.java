package com.zaikabharat.services;

import com.zaikabharat.db.DatabaseManager;
import com.zaikabharat.models.Recipe;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeService {

    private final List<Recipe> all = new ArrayList<>();

    public RecipeService() { loadAll(); }

    // ── Data loading ─────────────────────────────────────────────────────────

    private void loadAll() {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM recipes ORDER BY name")) {
            while (rs.next()) all.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error loading recipes: " + e.getMessage());
        }
    }

    private Recipe map(ResultSet rs) throws SQLException {
        return new Recipe(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("info"),
            rs.getString("origin"),
            rs.getString("total_time"),
            split(rs.getString("ingredients")),
            split(rs.getString("tags")),
            rs.getString("preparation_steps"),
            rs.getString("cook_steps"),
            rs.getString("recipe"),
            rs.getString("best_serving_with"),
            rs.getString("type"),
            rs.getInt("is_veg") == 1,
            rs.getInt("calories"),
            rs.getInt("protein_g"),
            rs.getInt("carbs_g"),
            rs.getInt("fiber_g"),
            rs.getInt("fat_g"),
            rs.getInt("spice_level"),
            rs.getString("difficulty")
        );
    }

    private List<String> split(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                     .map(String::trim).filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Recipe> getAll() { return Collections.unmodifiableList(all); }

    public List<Recipe> getByRegion(String regionTag) {
        return all.stream()
            .filter(r -> r.getTags().stream()
                          .anyMatch(t -> t.equalsIgnoreCase(regionTag)))
            .collect(Collectors.toList());
    }

    /** Full search + multi-filter. Pass empty/null strings for "no filter". */
    public List<Recipe> search(String query, boolean vegOnly,
                                String region, String cookTimeFilter, int spiceLevel) {
        String q = (query == null ? "" : query.toLowerCase().trim());
        return all.stream()
            .filter(r -> q.isEmpty()
                || r.getName().toLowerCase().contains(q)
                || r.getInfo().toLowerCase().contains(q)
                || r.getOrigin().toLowerCase().contains(q)
                || r.getIngredients().stream().anyMatch(i -> i.toLowerCase().contains(q)))
            .filter(r -> !vegOnly || r.isVeg())
            .filter(r -> region == null || region.equals("all")
                || r.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(region)))
            .filter(r -> matchCookTime(r.getTotalTime(), cookTimeFilter))
            .filter(r -> spiceLevel == 0 || r.getSpiceLevel() == spiceLevel)
            .collect(Collectors.toList());
    }

    private boolean matchCookTime(String totalTime, String filter) {
        if (filter == null || filter.equals("any")) return true;
        int m = parseMinutes(totalTime);
        return switch (filter) {
            case "quick" -> m <= 15;
            case "30min" -> m <= 30;
            case "60min" -> m <= 60;
            case "slow"  -> m > 60;
            default      -> true;
        };
    }

    private int parseMinutes(String t) {
        if (t == null) return 60;
        t = t.toLowerCase();
        try { return Integer.parseInt(t.replaceAll("[^0-9].*", "").trim()); }
        catch (Exception e) { return 60; }
    }

    /** Smart Fridge 2.0 — returns matches sorted by % descending. */
    public List<FridgeMatch> matchByIngredients(List<String> selected) {
        return all.stream()
            .map(r -> {
                long matched = r.getIngredients().stream()
                    .filter(ing -> selected.stream()
                                          .anyMatch(s -> s.equalsIgnoreCase(ing.trim())))
                    .count();
                List<String> missing = r.getIngredients().stream()
                    .filter(ing -> selected.stream()
                                          .noneMatch(s -> s.equalsIgnoreCase(ing.trim())))
                    .collect(Collectors.toList());
                int total   = r.getIngredients().size();
                int percent = total > 0 ? (int)(matched * 100L / total) : 0;
                return new FridgeMatch(r, percent, (int)matched, total, missing);
            })
            .filter(m -> m.matchPercent > 0)
            .sorted(Comparator.comparingInt(FridgeMatch::getMatchPercent).reversed())
            .collect(Collectors.toList());
    }

    /** Unique ingredient list (excluding pantry staples) for the Fridge chip grid. */
    public List<String> getAllIngredients() {
        Set<String> exclude = Set.of("water","salt","oil","ghee","sugar","spices",
                                     "black salt","turmeric","red chili powder","asafoetida");
        TreeSet<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        all.forEach(r -> r.getIngredients().forEach(i -> {
            if (!exclude.contains(i.trim().toLowerCase())) seen.add(i.trim());
        }));
        return new ArrayList<>(seen);
    }

    // ── Inner record ─────────────────────────────────────────────────────────

    public record FridgeMatch(Recipe recipe, int matchPercent, int matchCount,
                               int totalCount, List<String> missingIngredients) {
        public int getMatchPercent() { return matchPercent; }
        public boolean canCook()     { return matchPercent == 100; }
    }
}
