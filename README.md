# 🍽 ZaikaBharat v2.0 — GUI Edition
> *Tales Woven in Flavors*

A **JavaFX 21** desktop app for Indian recipe discovery.
Upgraded from the original CLI, now featuring a full **Glassmorphism UI**
with five new features on top of the original Browse / Categories / Fridge core.

---

## ✨ New in v2.0

| Feature | Description |
|---|---|
| **Nutritional Snapshot** | Per-serving bars for Calories, Protein, Carbs, Fiber, Fat — scales with serving count |
| **Cook Mode** | Step-by-step guided cooking with animated countdown timer per step |
| **Smart Fridge 2.0** | % match scoring, sorted results, "Missing: X, Y" hints |
| **Smart Search & Filters** | Live full-text search + region / cook-time / spice level multi-filter |
| **India Map Discovery** | Interactive SVG map — click a region to explore its recipes |

---

## 🛠 Prerequisites

| Tool | Version |
|---|---|
| Java JDK | **21** (Temurin / Oracle) |
| Maven | 3.9+ |

> No MySQL needed — the app uses an **embedded SQLite** database
> created automatically at `~/.zaikabharat/recipes.db` on first launch.

---

## 🚀 Running the App

```bash
# 1. Clone / unzip the project
cd zaikabharat-gui

# 2. Run directly (downloads deps on first run)
mvn javafx:run

# 3. Build a fat-jar
mvn package
java -jar target/zaikabharat-gui-2.0.0.jar
```

---

## 📁 Project Structure

```
zaikabharat-gui/
├── pom.xml
└── src/main/
    ├── java/com/zaikabharat/
    │   ├── ZaikaBharatApp.java          ← entry point
    │   ├── db/DatabaseManager.java      ← SQLite + seed data (20 recipes)
    │   ├── models/Recipe.java           ← model + CookStep record
    │   ├── services/RecipeService.java  ← search, filter, Smart Fridge engine
    │   └── ui/
    │       ├── MainWindow.java          ← sidebar + content area
    │       ├── Navigator.java           ← fade-transition routing
    │       ├── screens/
    │       │   ├── HomeScreen.java      ← browse + category pills
    │       │   ├── SearchScreen.java    ← live search + multi-filter
    │       │   ├── FridgeScreen.java    ← Smart Fridge 2.0
    │       │   ├── RecipeDetailScreen.java
    │       │   ├── CookModeScreen.java  ← step timer + arc dial
    │       │   └── MapScreen.java       ← India Map (WebView + JS bridge)
    │       └── components/
    │           ├── RecipeCard.java
    │           └── NutritionPanel.java
    └── resources/com/zaikabharat/
        ├── css/glass.css               ← full glassmorphism stylesheet
        └── web/india-map.html          ← SVG India map with JS↔Java bridge
```

---

## 🎨 Design System

- **Background** — deep indigo-to-purple gradient
- **Panels** — `rgba(255,255,255, 0.06–0.10)` glass with subtle white border
- **Accent** — Saffron `#FF9933`  |  Gold `#E8A838`
- **Veg** — `#4ECC6A`  |  **Non-veg** — `#FF5252`
- **Framework** — JavaFX 21 CSS (no web dependencies)

---

## 📦 Dependencies

| Library | Purpose |
|---|---|
| `javafx-controls`, `javafx-web` | UI framework |
| `atlantafx-base 2.0.1` | Modern JavaFX base theme |
| `sqlite-jdbc 3.45.1.0` | Embedded database |

---

## 👥 Team

Original CLI — ZaikaBharat Team  
GUI Upgrade — ZaikaBharat v2.0
