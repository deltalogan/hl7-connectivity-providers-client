package com.hl7client.model.dental;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Define las reglas de validación para combinaciones válidas de superficies dentales
 * según el tipo de pieza (basado en estándares FDI/ISO 3950 y observaciones clínicas).
 * <p>
 * Reglas implementadas:
 * <ul>
 *   <li>Dientes anteriores (incisivos, caninos): M, D, V, L, I, P</li>
 *   <li>Dientes posteriores (premolares, molares): M, D, V, L, O, P</li>
 *   <li>Máximo 3 superficies por prestación</li>
 *   <li>No se permite combinar Oclusal (O) e Incisal (I)</li>
 *   <li>Sin pieza → no se permiten superficies</li>
 * </ul>
 */
public final class DentalSurfaceMatrix {

    // Conjuntos de superficies permitidas por grupo de dientes
    private static final Set<DentalSurface> ANTERIORES = EnumSet.of(
            DentalSurface.MESIAL,
            DentalSurface.DISTAL,
            DentalSurface.VESTIBULAR,
            DentalSurface.LINGUAL,
            DentalSurface.INCISAL,
            DentalSurface.PALATAL
    );

    private static final Set<DentalSurface> POSTERIORES = EnumSet.of(
            DentalSurface.MESIAL,
            DentalSurface.DISTAL,
            DentalSurface.VESTIBULAR,
            DentalSurface.LINGUAL,
            DentalSurface.OCCLUSAL,
            DentalSurface.PALATAL
    );

    // Reglas globales
    private static final int MAX_SURFACES_ALLOWED = 3;
    private static final Set<DentalSurface> MUTUALLY_EXCLUSIVE = EnumSet.of(
            DentalSurface.OCCLUSAL,
            DentalSurface.INCISAL
    );

    /**
     * Retorna el conjunto de superficies permitidas para una pieza dada.
     *
     * @param piece pieza dental (puede ser null)
     * @return conjunto inmutable de superficies permitidas (vacío si piece == null)
     */
    public static Set<DentalSurface> getAllowedSurfacesFor(DentalPiece piece) {
        if (piece == null) {
            return EnumSet.noneOf(DentalSurface.class);
        }

        DentalPieceType type = piece.getType();

        switch (type) {
            case INCISIVO:
            case CANINO:
                return ANTERIORES;
            case PREMOLAR:
            case MOLAR:
                return POSTERIORES;
            default:
                return EnumSet.noneOf(DentalSurface.class);
        }
    }

    /**
     * Valida si la combinación de pieza + superficies es odontológicamente válida.
     * <p>
     * Reglas aplicadas (estrictas):
     * <ul>
     *   <li>Sin pieza → superficies deben estar vacías</li>
     *   <li>Con pieza → todas las superficies deben estar permitidas para ese tipo</li>
     *   <li>Máximo {@value #MAX_SURFACES_ALLOWED} superficies</li>
     *   <li>No combinar Oclusal e Incisal</li>
     * </ul>
     *
     * @param piece             pieza seleccionada (puede ser null)
     * @param selectedSurfaces  conjunto de superficies marcadas (no null)
     * @return resultado de validación (puede contener múltiples mensajes de error)
     */
    public static DentalValidationResult validate(
            DentalPiece piece,
            Set<DentalSurface> selectedSurfaces
    ) {
        if (selectedSurfaces == null) {
            selectedSurfaces = EnumSet.noneOf(DentalSurface.class);
        }

        List<String> errors = new ArrayList<>();

        // Regla 1: sin pieza → prohibido tener superficies
        if (piece == null) {
            if (!selectedSurfaces.isEmpty()) {
                errors.add("No se pueden seleccionar superficies sin elegir una pieza dental.");
            }
            return buildResult(errors);
        }

        // Regla 2: superficies permitidas según tipo de diente
        Set<DentalSurface> allowed = getAllowedSurfacesFor(piece);
        for (DentalSurface surface : selectedSurfaces) {
            if (!allowed.contains(surface)) {
                errors.add(String.format(
                        "La superficie %s (%s) no está permitida en la pieza %s (%s)",
                        surface.name(), surface.getCode(),
                        piece.getFdiCode(), piece.getType().name().toLowerCase()
                ));
            }
        }

        // Regla 3: límite máximo de superficies
        if (selectedSurfaces.size() > MAX_SURFACES_ALLOWED) {
            errors.add(String.format(
                    "Máximo permitido: %d superficies por prestación (seleccionadas: %d)",
                    MAX_SURFACES_ALLOWED, selectedSurfaces.size()
            ));
        }

        // Regla 4: incompatibilidades explícitas
        if (selectedSurfaces.containsAll(MUTUALLY_EXCLUSIVE)) {
            errors.add("No se puede combinar Oclusal (O) e Incisal (I) en la misma prestación.");
        }

        // → Aquí se pueden agregar más reglas específicas en el futuro

        return buildResult(errors);
    }

    private static DentalValidationResult buildResult(List<String> errors) {
        return errors.isEmpty()
                ? DentalValidationResult.ok()
                : DentalValidationResult.errors(errors);
    }

    private DentalSurfaceMatrix() {
        // Clase utilitaria → no instanciable
    }
}