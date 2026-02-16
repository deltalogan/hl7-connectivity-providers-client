package com.hl7client;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

    public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Application().start();
            } catch (Exception e) {
                showFatalError(e);
            }
        });
    }

    private static void showFatalError(Exception e) {
        LOGGER.log(Level.SEVERE, "Error fatal al iniciar la aplicación", e);

        String detail = (e.getMessage() != null && !e.getMessage().isEmpty())
                ? e.getMessage()
                : e.getClass().getSimpleName();

        JOptionPane.showMessageDialog(
                null,
                "Error fatal al iniciar la aplicación.\n" +
                        "Detalle: " + detail + "\n\n" +
                        "Consulte los logs para más información.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        System.exit(1);
    }
}
