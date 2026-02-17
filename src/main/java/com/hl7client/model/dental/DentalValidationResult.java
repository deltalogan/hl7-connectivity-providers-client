package com.hl7client.model.dental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Resultado de una validación odontológica.
 * Permite acumular múltiples mensajes de error cuando hay varias violaciones de reglas.
 */
public final class DentalValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public DentalValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static DentalValidationResult ok() {
        return new DentalValidationResult(true, Collections.emptyList());
    }

    public static DentalValidationResult error(String message) {
        Objects.requireNonNull(message, "El mensaje de error no puede ser null");
        List<String> errors = new ArrayList<>();
        errors.add(message);
        return new DentalValidationResult(false, errors);
    }

    public static DentalValidationResult errors(List<String> errorMessages) {
        Objects.requireNonNull(errorMessages, "La lista de errores no puede ser null");
        if (errorMessages.isEmpty()) {
            return ok();
        }
        return new DentalValidationResult(false, errorMessages);
    }

    /**
     * Indica si la validación **falló** (hay al menos un error).
     */
    public boolean hasErrors() {
        return valid;   // ← Corrección clave aquí
    }

    /**
     * Retorna la lista de mensajes de error (inmutable).
     * Está vacía si !hasErrors()
     */
    @SuppressWarnings("unused")
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Retorna un único mensaje combinado (útil para mostrar en JOptionPane, etc.).
     */
    public String getMessage() {
        if (hasErrors()) {               // ← ajustado para usar el nuevo método
            return "Validación exitosa";
        }
        if (errors.size() == 1) {
            return errors.get(0);
        }
        return String.join("\n• ", errors);
    }

    /**
     * Versión HTML del mensaje (ideal para mostrar en diálogos o tooltips).
     */
    @SuppressWarnings("unused")
    public String getHtmlMessage() {
        if (hasErrors()) {               // ← ajustado
            return "<html><font color='green'>Validación exitosa</font></html>";
        }
        return "<html><font color='red'>" + "Errores de validación:<br>• " +
                String.join("<br>• ", errors) +
                "</font></html>";
    }

    @Override
    public String toString() {
        return "DentalValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}