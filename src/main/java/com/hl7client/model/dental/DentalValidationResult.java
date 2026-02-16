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

    /**
     * Crea un resultado de validación exitosa (sin errores).
     */
    public static DentalValidationResult ok() {
        return new DentalValidationResult(true, Collections.emptyList());
    }

    /**
     * Crea un resultado de validación con un único error.
     */
    public static DentalValidationResult error(String message) {
        Objects.requireNonNull(message, "El mensaje de error no puede ser null");
        List<String> errors = new ArrayList<>();
        errors.add(message);
        return new DentalValidationResult(false, errors);
    }

    /**
     * Crea un resultado de validación con múltiples errores.
     */
    public static DentalValidationResult errors(List<String> errorMessages) {
        Objects.requireNonNull(errorMessages, "La lista de errores no puede ser null");
        if (errorMessages.isEmpty()) {
            return ok();
        }
        return new DentalValidationResult(false, errorMessages);
    }

    /**
     * Indica si la validación fue exitosa (sin errores).
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Retorna la lista de mensajes de error (inmutable).
     * Está vacía si isValid() == true.
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Retorna un único mensaje combinado (útil para mostrar en JOptionPane, etc.).
     * Si no hay errores, retorna "Validación exitosa".
     */
    public String getMessage() {
        if (valid) {
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
    public String getHtmlMessage() {
        if (valid) {
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