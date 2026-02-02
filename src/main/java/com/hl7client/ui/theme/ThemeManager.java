package com.hl7client.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.prefs.Preferences;

public final class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String PREF_KEY = "ui.theme.mode";

    private final Preferences prefs =
            Preferences.userNodeForPackage(ThemeManager.class);

    private Theme currentTheme;

    // -------------------- Singleton --------------------

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    // -------------------- Public API --------------------

    /**
     * Debe llamarse UNA vez al inicio de la aplicación,
     * antes de crear cualquier JFrame.
     */
    public void initialize() {

        // 1️⃣ Preferencia del usuario
        Theme stored = loadStoredTheme();
        if (stored != null) {
            applyTheme(stored);
            return;
        }

        // 2️⃣ Tema del sistema operativo
        Optional<Boolean> systemDark = SystemThemeDetector.isDark();
        if (systemDark.isPresent()) {
            applyTheme(systemDark.get() ? Theme.DARK : Theme.LIGHT);
            return;
        }

        // 3️⃣ Fallback seguro
        applyTheme(Theme.LIGHT);
    }

    public void toggleTheme() {
        Theme next = (currentTheme == Theme.LIGHT)
                ? Theme.DARK
                : Theme.LIGHT;

        saveUserPreference(next);
        applyTheme(next);
    }

    public Icon getToggleIcon() {
        return currentTheme == Theme.LIGHT
                ? Icons.MOON
                : Icons.SUN;
    }

    // -------------------- Internal logic --------------------

    private Theme loadStoredTheme() {
        String stored = prefs.get(PREF_KEY, null);
        return stored != null ? Theme.valueOf(stored) : null;
    }

    private void saveUserPreference(Theme theme) {
        prefs.put(PREF_KEY, theme.name());
    }

    private void applyTheme(Theme theme) {
        try {
            if (theme == Theme.DARK) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            currentTheme = theme;
            updateAllWindows();

        } catch (Exception e) {
            throw new RuntimeException("Error aplicando tema", e);
        }
    }

    private void updateAllWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }

    // -------------------- Internal enum --------------------

    public enum Theme {
        LIGHT,
        DARK
    }

    // -------------------- Icons --------------------

    private static final class Icons {

        static final Icon SUN =
                new FlatSVGIcon("icons/sun.svg", 16, 16);

        static final Icon MOON =
                new FlatSVGIcon("icons/moon.svg", 16, 16);

        private Icons() {
        }
    }

}
