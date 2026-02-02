package com.hl7client.controller;

import com.hl7client.ui.dialogs.SplashDialog;
import com.hl7client.ui.splash.BusyCursorManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public final class SplashController {

    private final Window owner;
    private final BusyCursorManager cursorManager;
    private SplashDialog dialog;

    public SplashController(Window owner) {
        this.owner = owner;
        this.cursorManager = new BusyCursorManager(owner);
    }

    public void show(URL gif) {
        cursorManager.activate();

        dialog = new SplashDialog(owner, gif);

        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        dialog.setCursor(wait);

        // Como JDialog YA es RootPaneContainer, usamos el glassPane directamente
        JComponent glass = (JComponent) dialog.getGlassPane();
        glass.setCursor(wait);
        glass.setVisible(true);

        new Thread(() -> dialog.setVisible(true), "SplashDialog").start();
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null) {
                JComponent glass = (JComponent) dialog.getGlassPane();
                glass.setVisible(false);

                dialog.setCursor(Cursor.getDefaultCursor()); // restaurar splash
                dialog.dispose();
                dialog = null;
            }
            cursorManager.restore();
        });
    }
}
