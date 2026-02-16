package com.hl7client.ui.util;

import com.hl7client.Application;
import com.hl7client.ApplicationCloseIntent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class CloseAction extends AbstractAction {

    private final Application application;
    private final ApplicationCloseIntent intent;

    /**
     * Acción genérica para cerrar la ventana o salir de la aplicación.
     *
     * @param name        Texto visible del botón o menú
     * @param tooltip     Texto de ayuda (tooltip)
     * @param application Aplicación principal
     * @param intent      Intención de cierre
     */
    public CloseAction(
            String name,
            String tooltip,
            Application application,
            ApplicationCloseIntent intent
    ) {
        super(name);

        this.application = Objects.requireNonNull(application, "application");
        this.intent = Objects.requireNonNull(intent, "intent");

        if (tooltip != null && !tooltip.isEmpty()) {
            putValue(Action.SHORT_DESCRIPTION, tooltip);
        }
    }

    /**
     * Constructor de conveniencia con valores por defecto.
     * Útil para botones simples como "Cancel".
     */
    public CloseAction(
            Application application,
            ApplicationCloseIntent intent
    ) {
        this("Cerrar", "Cerrar", application, intent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        application.requestClose(intent);
    }
}
