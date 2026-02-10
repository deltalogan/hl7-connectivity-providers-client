package com.hl7client.model.dental;

import java.util.Set;

public final class DentalValidator {

    public static void validate(
            DentalPiece piece,
            Set<DentalSurface> surfaces
    ) {
        if (surfaces == null || surfaces.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos una cara dental"
            );
        }

        Set<DentalSurface> allowed =
                DentalSurfaceMatrix.allowedFor(piece);

        for (DentalSurface surface : surfaces) {
            if (!allowed.contains(surface)) {
                throw new IllegalArgumentException(
                        "La cara " + surface +
                                " no es válida para la pieza " + piece.getFdiCode()
                );
            }
        }
    }

    private DentalValidator() {}
}
