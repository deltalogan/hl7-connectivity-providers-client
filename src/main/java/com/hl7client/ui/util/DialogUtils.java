package com.hl7client.ui.util;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class DialogUtils {

    private DialogUtils() {
    }

    /**
     * Instala una Action de cierre común para un JDialog:
     * - ESC
     * - Botón Cancel
     * - X de la ventana
     * - ALT+F4
     */
    public static void installCloseAction(
            JDialog dialog,
            Action closeAction
    ) {
        // ESC
        dialog.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");

        dialog.getRootPane()
                .getActionMap()
                .put("close", closeAction);

        // X de la ventana + ALT+F4
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAction.actionPerformed(null);
            }
        });
    }

    /**
     * Action estándar para cerrar un dialog.
     */
    public static Action createDisposeAction(JDialog dialog) {
        return new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        };
    }
}
