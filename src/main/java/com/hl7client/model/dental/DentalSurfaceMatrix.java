package com.hl7client.model.dental;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Matriz de superficies dentales permitidas por tipo de pieza.
 * Basada en estándares odontológicos (FDI/ISO 3950) y observaciones de datos reales de producción.
 * <p>
 * Reglas principales:
 * - Dientes anteriores (incisivos y caninos): M, D, V, L, I, P
 * - Dientes posteriores (premolares y molares): M, D, V, L, O, P
 * - Máximo 3 superficies por prestación
 * - No se permite combinar Oclusal (O) e Incisal (I)
 */
public final class DentalSurfaceMatrix {

    // Superficies permitidas para dientes anteriores (incisivos y caninos)
    private static final Set<DentalSurface> ANTERIORES =
            EnumSet.of(
                    DentalSurface.MESIAL,      // M
                    DentalSurface.DISTAL,      // D
                    DentalSurface.VESTIBULAR,  // V
                    DentalSurface.LINGUAL,     // L
                    DentalSurface.INCISAL,     // I
                    DentalSurface.PALATAL      // P
            );

    // Superficies permitidas para dientes posteriores (premolares y molares)
    private static final Set<DentalSurface> POSTERIORES =
            EnumSet.of(
                    DentalSurface.MESIAL,      // M
                    DentalSurface.DISTAL,      // D
                    DentalSurface.VESTIBULAR,  // V
                    DentalSurface.LINGUAL,     // L
                    DentalSurface.OCCLUSAL,    // O
                    DentalSurface.PALATAL      // P
            );

    /**
     * Retorna el conjunto de superficies permitidas para una pieza dental específica.
     *
     * @param piece la pieza dental (puede ser null)
     * @return conjunto inmutable de superficies permitidas (vacío si piece == null)
     */
    public static Set<DentalSurface> allowedFor(DentalPiece piece) {
        if (piece == null) {
            return EnumSet.noneOf(DentalSurface.class);
        }
        return switch (piece.getType()) {
            case INCISIVO, CANINO -> ANTERIORES;
            case PREMOLAR, MOLAR  -> POSTERIORES;
        };
    }

    /**
     * Valida si una combinación de superficies es válida para una pieza dada.
     * Aplica todas las reglas observadas en producción y estándares odontológicos.
     * <p>
     * Devuelve un {@link DentalValidationResult} que puede contener múltiples errores.
     *
     * @param piece            la pieza dental seleccionada (puede ser null)
     * @param selectedSurfaces las superficies seleccionadas (puede ser null o vacío)
     * @return resultado de la validación con posible acumulación de mensajes de error
     */
    public static DentalValidationResult validateCombination(
            DentalPiece piece,
            Set<DentalSurface> selectedSurfaces
    ) {
        List<String> errors = new ArrayList<>();

        // Caso 1: sin pieza
        if (piece == null) {
            if (!selectedSurfaces.isEmpty()) {
                errors.add("No se pueden seleccionar superficies si no hay pieza dental seleccionada");
            }
            // Permitimos explícitamente pieza null + superficies vacías
            return errors.isEmpty() ? DentalValidationResult.ok() : DentalValidationResult.errors(errors);
        }

        // Caso 2: con pieza → ya NO exigimos al menos una superficie
        // (aceptamos 0 o más)
        Set<DentalSurface> allowed = allowedFor(piece);

        for (DentalSurface surface : selectedSurfaces) {
            if (!allowed.contains(surface)) {
                errors.add(String.format(
                        "La superficie %s (%s) no está permitida para la pieza %s",
                        surface.name(), surface.getCode(), piece.getFdiCode()
                ));
            }
        }

        // Regla: máximo 3 superficies (mantener si el negocio lo exige)
        if (selectedSurfaces.size() > 3) {
            errors.add("Máximo permitido: 3 superficies por prestación (actual: " + selectedSurfaces.size() + ")");
        }

        // Regla: incompatibilidad Oclusal + Incisal (mantener)
        if (selectedSurfaces.contains(DentalSurface.OCCLUSAL) &&
                selectedSurfaces.contains(DentalSurface.INCISAL)) {
            errors.add("No se puede combinar Oclusal (O) e Incisal (I) en la misma prestación");
        }

        return errors.isEmpty()
                ? DentalValidationResult.ok()
                : DentalValidationResult.errors(errors);
    }

    private DentalSurfaceMatrix() {
        // Evitar instanciación
    }
}