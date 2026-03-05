package Application;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class ThemeManager {
    private static boolean isDarkMode = false;
    private static Scene currentScene;

    public static void setScene(Scene scene) {
        currentScene = scene;
        applyTheme(scene);
    }

    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (currentScene != null) applyTheme(currentScene);
    }

    public static void toggleTheme(Node node) {
        isDarkMode = !isDarkMode;
        Scene scene = node.getScene();
        if (scene != null) {
            currentScene = scene;
            applyTheme(scene);
        }
    }

    public static void applyTheme(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            if (isDarkMode) {
                scene.getRoot().getStyleClass().add("dark-mode");
                scene.getRoot().getStyleClass().remove("light-mode");
            } else {
                scene.getRoot().getStyleClass().add("light-mode");
                scene.getRoot().getStyleClass().remove("dark-mode");
            }
        }
    }

    public static boolean isDarkMode() { return isDarkMode; }

    public static Button createThemeToggleButton() {
        Button b = new Button(isDarkMode ? "☀️ Clair" : "🌙 Sombre");
        b.getStyleClass().add("theme-toggle-btn");
        b.setOnAction(e -> {
            toggleTheme(b);
            b.setText(isDarkMode ? "☀️ Clair" : "🌙 Sombre");
        });
        return b;
    }
}
