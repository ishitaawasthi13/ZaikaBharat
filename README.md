# ZaikaBharat
> *Tales Woven in Flavors*

Zaika Bharat is a **JavaFX 21** desktop app for Indian recipe discovery. It is designed to catalog and showcase a vast library of Indian recipes from every corner of the country, both historical and contemporary. It features a full **Glassmorphism UI**. 

---

## Features

| Feature | Description |
|---|---|
| **Nutritional Snapshot** | Per-serving bars for Calories, Protein, Carbs, Fiber, Fat - scales with serving count |
| **Cook Mode** | Step by step guided cooking with animated countdown timer per step |
| **Smart Fridge** | % match scoring, sorted results, "Missing: X, Y" hints |
| **Smart Search & Filters** | Live full text search + region / cook time / spice level multi filter |
| **India Map Discovery** | Interactive SVG map — click a region to explore its recipes |

---

## Prerequisites

| Tool | Version |
|---|---|
| Java JDK | **21** |
| Maven | 3.9+ |

> No MySQL needed — the app uses an **embedded SQLite** database
> created automatically at `~/.zaikabharat/recipes.db` on first launch.

---

## Running the App

```bash
# 1. Clone the project
git clone https://github.com/ishitaawasthi13/ZaikaBharat.git

# 2. Run directly (downloads deps on first run)
mvn javafx:run

# 3. Build a fat-jar
mvn package
java -jar target/zaikabharat.jar
```

---

## Dependencies

| Library | Purpose |
|---|---|
| `javafx-controls`, `javafx-web` | UI framework |
| `atlantafx-base 2.0.1` | Modern JavaFX base theme |
| `sqlite-jdbc 3.45.1.0` | Embedded database |

---
