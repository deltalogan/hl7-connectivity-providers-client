package com.hl7client.model.dental;

import java.util.EnumSet;
import java.util.Set;

/**
 * Superficies dentales según notación usada en el sistema HL7.
 * Los códigos coinciden con los observados en producción.
 */
public enum DentalSurface {

    MESIAL("M"),
    DISTAL("D"),
    VESTIBULAR("V"),     // Código V = Vestibular / Facial (como aparece en producción)
    LINGUAL("L"),
    OCCLUSAL("O"),
    INCISAL("I"),
    PALATAL("P");        // Agregado: P = Palatal

    private final String code;

    DentalSurface(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Busca una superficie por su código (case-sensitive).
     * @return la superficie correspondiente o null si no existe
     */
    public static DentalSurface fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (DentalSurface surface : values()) {
            if (surface.code.equals(code)) {
                return surface;
            }
        }
        return null;
    }

    /**
     * Versión conveniente para convertir un string de superficies (ej: "VML")
     * en conjunto de DentalSurface.
     */
    @SuppressWarnings("unused")
    public static Set<DentalSurface> fromCodes(String codes) {
        Set<DentalSurface> result = EnumSet.noneOf(DentalSurface.class);
        if (codes == null || codes.isEmpty()) {
            return result;
        }
        for (char c : codes.toCharArray()) {
            DentalSurface surface = fromCode(String.valueOf(c));
            if (surface != null) {
                result.add(surface);
            }
        }
        return result;
    }
}