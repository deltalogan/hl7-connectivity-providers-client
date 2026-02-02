package com.hl7client.ui.util;

import java.awt.*;

public final class WindowSizer {

    private WindowSizer() {
    }

    /**
     * Aplica un tamaño relativo a la pantalla disponible.
     *
     * @param window ventana a dimensionar
     * @param ratio  porcentaje de la pantalla (0.0 – 1.0)
     */
    public static void applyRelativeScreenSize(Window window, double ratio) {

        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int usableWidth = bounds.width - insets.left - insets.right;
        int usableHeight = bounds.height - insets.top - insets.bottom;

        window.setSize(
                (int) (usableWidth * ratio),
                (int) (usableHeight * ratio)
        );
    }
}
