package com.hl7client.ui.theme;

import javax.swing.*;

public final class ThemeButtonBinder {

    private ThemeButtonBinder() {
    }

    public static void bind(JButton button) {
        ThemeManager themeManager = ThemeManager.getInstance();

        // Ícono inicial
        button.setIcon(themeManager.getToggleIcon());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        // Acción
        button.addActionListener(e -> {
            themeManager.toggleTheme();
            button.setIcon(themeManager.getToggleIcon());
        });
    }
}
