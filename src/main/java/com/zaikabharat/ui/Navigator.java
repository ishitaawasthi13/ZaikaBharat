package com.zaikabharat.ui;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

public class Navigator {

    private static StackPane content;
    private static final Deque<Node> history = new ArrayDeque<>();

    public static void init(StackPane pane) { content = pane; }

    public static void navigateTo(Node screen) { navigateTo(screen, true); }

    public static void navigateTo(Node screen, boolean addToHistory) {
        if (!content.getChildren().isEmpty() && addToHistory)
            history.push(content.getChildren().get(0));
        screen.setOpacity(0);
        content.getChildren().setAll(screen);
        FadeTransition ft = new FadeTransition(Duration.millis(220), screen);
        ft.setToValue(1);
        ft.play();
    }

    public static void goBack() {
        if (!history.isEmpty()) {
            Node prev = history.pop();
            prev.setOpacity(0);
            content.getChildren().setAll(prev);
            FadeTransition ft = new FadeTransition(Duration.millis(220), prev);
            ft.setToValue(1);
            ft.play();
        }
    }

    public static void clearHistory() { history.clear(); }
    public static boolean canGoBack() { return !history.isEmpty(); }
}
