package com.hl7client.ui.splash;

import java.awt.*;
import javax.swing.*;

public final class BusyCursorManager {

    private Cursor originalCursor;
    private final Window window;

    public BusyCursorManager(Window window) {
        this.window = window;
    }

    public void activate() {
        originalCursor = window.getCursor();
        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

        window.setCursor(wait);

        if (window instanceof RootPaneContainer rpc) {
            JComponent glass = (JComponent) rpc.getGlassPane();
            glass.setCursor(wait);
            glass.setVisible(true);
        }
    }

    public void restore() {
        if (originalCursor == null) return;

        window.setCursor(originalCursor);

        if (window instanceof RootPaneContainer rpc) {
            JComponent glass = (JComponent) rpc.getGlassPane();
            glass.setVisible(false);
        }
    }
}
