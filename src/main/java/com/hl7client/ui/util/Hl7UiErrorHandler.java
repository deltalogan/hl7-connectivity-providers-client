package com.hl7client.ui.util;

import com.hl7client.model.result.Hl7Error;
import com.hl7client.model.result.Hl7ItemError;
import com.hl7client.model.result.Hl7Result;
import com.hl7client.model.result.Hl7Status;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class Hl7UiErrorHandler {

    private Hl7UiErrorHandler() {
        // utility class
    }

    // =====================================================
    // ========== ENTRADA ÚNICA DESDE LA UI ================
    // =====================================================

    public static void mostrarResultado(
            Component parent,
            Hl7Result<?> result,
            String contexto,
            String transac
    ) {

        if (result == null) {
            mostrarErrorTecnico(parent, contexto, transac);
            return;
        }

        Hl7Status status = result.getStatus();

        switch (status) {
            case OK:
                // no se muestra nada
                break;

            case PARTIAL:
                mostrarResultadoParcial(
                        parent,
                        result.getDetails(),
                        contexto,
                        transac
                );
                break;

            case REJECTED:
                mostrarErrorFuncional(
                        parent,
                        result.getIssue().orElse(null),
                        result.getDetails(),
                        contexto,
                        transac
                );
                break;

            case ERROR:
                mostrarErrorBloqueante(
                        parent,
                        result.getIssue().orElse(null),
                        contexto,
                        transac
                );
                break;

            default:
                // Caso inesperado (defensivo)
                mostrarErrorTecnico(parent, "Estado desconocido: " + status, contexto, transac);
                break;
        }
    }

    // =====================================================
    // ========== RESULTADO PARCIAL ========================
    // =====================================================

    private static void mostrarResultadoParcial(
            Component parent,
            List<Hl7ItemError> details,
            String contexto,
            String transac
    ) {

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("La operación se realizó con observaciones.");

        if (details != null && !details.isEmpty()) {
            mensaje.append("\n\nDetalle:");
            for (Hl7ItemError d : details) {
                mensaje.append("\n- ")
                        .append(d.getMessage())
                        .append(" (")
                        .append(d.getCode())
                        .append(")");
            }
        }

        appendTransaccion(mensaje, transac);

        JOptionPane.showMessageDialog(
                parent,
                mensaje.toString(),
                "Resultado parcial de " + contexto,
                JOptionPane.WARNING_MESSAGE
        );
    }

    // =====================================================
    // ========== ERRORES FUNCIONALES (REJECTED) ===========
    // =====================================================

    private static void mostrarErrorFuncional(
            Component parent,
            Hl7Error error,
            List<Hl7ItemError> details,
            String contexto,
            String transac
    ) {

        if (error == null) {
            mostrarErrorTecnico(parent, contexto, transac);
            return;
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append(error.getMessage());

        if (error.getCode() != null && !error.getCode().isEmpty()) {
            mensaje.append("\n\nCódigo: ").append(error.getCode());
        }

        if (details != null && !details.isEmpty()) {
            mensaje.append("\n\nDetalle:");
            for (Hl7ItemError d : details) {
                mensaje.append("\n- ")
                        .append(d.getMessage())
                        .append(" (")
                        .append(d.getCode())
                        .append(")");
            }
        }

        appendTransaccion(mensaje, transac);

        JOptionPane.showMessageDialog(
                parent,
                mensaje.toString(),
                "Operación rechazada - " + contexto,
                JOptionPane.WARNING_MESSAGE
        );
    }

    // =====================================================
    // ========== ERRORES BLOQUEANTES ======================
    // =====================================================

    private static void mostrarErrorBloqueante(
            Component parent,
            Hl7Error error,
            String contexto,
            String transac
    ) {

        if (error == null) {
            mostrarErrorTecnico(parent, contexto, transac);
            return;
        }

        if (error.isSession()) {
            mostrarSesionExpirada(parent, error);
            return;
        }

        mostrarErrorTecnico(
                parent,
                error.getMessage(),
                contexto,
                transac
        );
    }

    // =====================================================
    // ========== SESIÓN EXPIRADA ==========================
    // =====================================================

    private static void mostrarSesionExpirada(
            Component parent,
            Hl7Error error
    ) {
        JOptionPane.showMessageDialog(
                parent,
                error.getMessage(),
                "Sesión expirada",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // =====================================================
    // ========== ERRORES TÉCNICOS =========================
    // =====================================================

    private static void mostrarErrorTecnico(
            Component parent,
            String mensaje,
            String contexto,
            String transac
    ) {

        StringBuilder mensajeFinal = new StringBuilder(
                mensaje != null
                        ? mensaje
                        : "Error técnico al consultar " + contexto
        );

        appendTransaccion(mensajeFinal, transac);

        JOptionPane.showMessageDialog(
                parent,
                mensajeFinal.toString(),
                "Error técnico",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static void mostrarErrorTecnico(
            Component parent,
            String contexto,
            String transac
    ) {
        mostrarErrorTecnico(
                parent,
                "Error técnico al consultar " + contexto,
                contexto,
                transac
        );
    }

    // =====================================================
    // ========== SOPORTE ==================================
    // =====================================================

    private static void appendTransaccion(
            StringBuilder mensaje,
            String transac
    ) {
        if (transac != null) {
            mensaje.append("\n\nTransacción: ").append(transac);
        }
    }
}
