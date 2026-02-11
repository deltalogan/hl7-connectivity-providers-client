package com.hl7client.ui.util;

import java.awt.*;

/**
 * Utilidades para dimensionar y posicionar ventanas de forma consistente.
 */
public final class WindowSizer {

    private WindowSizer() {
        // Clase de utilidades → no instanciable
    }

    /**
     * Aplica un tamaño relativo a la pantalla disponible (tamaño inicial o preferred).
     *
     * @param window ventana a dimensionar
     * @param ratio  porcentaje de la pantalla usable (0.0 – 1.0)
     */
    public static void applyRelativeScreenSize(Window window, double ratio) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc == null) return;

        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int usableWidth = bounds.width - insets.left - insets.right;
        int usableHeight = bounds.height - insets.top - insets.bottom;

        window.setSize(
                (int) (usableWidth * ratio),
                (int) (usableHeight * ratio)
        );
    }

    /**
     * Establece un tamaño mínimo relativo a la pantalla disponible.
     * Útil para evitar que el usuario reduzca demasiado la ventana.
     *
     * @param window ventana a la que se le establecerá el tamaño mínimo
     * @param ratio  porcentaje de la pantalla usable (recomendado: 0.18–0.28)
     */
    public static void applyRelativeMinimumSize(Window window, double ratio) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc == null) return;

        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int usableWidth = bounds.width - insets.left - insets.right;
        int usableHeight = bounds.height - insets.top - insets.bottom;

        // Evitamos valores absurdamente pequeños
        int minWidth = Math.max(280, (int) (usableWidth * ratio));
        int minHeight = Math.max(220, (int) (usableHeight * ratio));

        window.setMinimumSize(new Dimension(minWidth, minHeight));
    }
}