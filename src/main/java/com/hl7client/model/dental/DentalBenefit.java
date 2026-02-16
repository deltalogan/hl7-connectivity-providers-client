package com.hl7client.model.dental;

import com.hl7client.model.benefit.BenefitItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Beneficio odontológico.
 * Es un beneficio único (orden 1) e inmutable.
 * El valor HL7 se construye al instanciarse con toda la información necesaria.
 * Permite pieza null (sin pieza dental) y superficies vacías.
 */
public final class DentalBenefit implements BenefitItem {

    private final DentalPiece piece;           // ahora puede ser null
    private final Set<DentalSurface> surfaces;
    private final String benefitCode;          // ej: "020801" (sin la O)
    private final String value;                // el string HL7 final

    public DentalBenefit(DentalPiece piece, Set<DentalSurface> surfaces, String benefitCode) {
        // Pieza es OPCIONAL → no lanzamos excepción si es null
        this.piece = piece;

        // Superficies opcionales (permitimos null o vacío)
        Set<DentalSurface> tempSurfaces = (surfaces != null) ? surfaces : EnumSet.noneOf(DentalSurface.class);
        this.surfaces = Collections.unmodifiableSet(EnumSet.copyOf(tempSurfaces));

        if (benefitCode == null || benefitCode.trim().isEmpty() || !benefitCode.matches("\\d+")) {
            throw new IllegalArgumentException("El código de prestación debe ser un número válido");
        }

        this.benefitCode = benefitCode.trim();
        this.value = buildValue();

        if (this.value.length() > 255) {
            throw new IllegalArgumentException("El valor HL7 generado excede el límite de 255 caracteres");
        }
    }

    /**
     * Método "with" para crear una copia con valores actualizados (ideal para edición)
     */
    public DentalBenefit with(DentalPiece newPiece, Set<DentalSurface> newSurfaces, String newBenefitCode) {
        return new DentalBenefit(
                newPiece != null ? newPiece : this.piece,
                newSurfaces != null && !newSurfaces.isEmpty() ? newSurfaces : this.surfaces,
                newBenefitCode != null && !newBenefitCode.trim().isEmpty() ? newBenefitCode : this.benefitCode
        );
    }

    public DentalPiece getPiece() {
        return piece;
    }

    public Set<DentalSurface> getSurfaces() {
        return surfaces;
    }

    public String getBenefitCode() {
        return benefitCode;
    }

    @Override
    public int getOrden() {
        return 1;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int length() {
        return value.length();
    }

    /**
     * Construye el string HL7 en el formato esperado.
     * Maneja correctamente pieza null y superficies vacías.
     */
    private String buildValue() {
        // Pieza: vacío si no hay pieza
        String pieceStr = (piece != null) ? piece.getFdiCode() : "";

        // Superficies: código concatenado o vacío, ordenado por prioridad
        String surfacesCode = surfaces.stream()
                .sorted(Comparator.comparingInt(DentalBenefit::getSurfacePriority))
                .map(DentalSurface::getCode)
                .collect(Collectors.joining());

        String codeWithPrefix = "O" + benefitCode;

        // Construcción explícita para garantizar separadores correctos
        StringBuilder sb = new StringBuilder();
        sb.append("1^*");               // total + separador cantidad-código
        sb.append(pieceStr);            // pieza (vacío si null)
        sb.append("*");                 // separador pieza → superficies
        sb.append(surfacesCode);        // superficies (vacío si ninguna)
        sb.append("*");                 // separador superficies → código
        sb.append(codeWithPrefix);      // código con prefijo O
        sb.append("*P*1**");            // origen P + cantidad 1 + terminador

        return sb.toString();
    }

    // Helper para ordenar superficies (reemplaza el switch expression)
    private static int getSurfacePriority(DentalSurface s) {
        switch (s) {
            case VESTIBULAR: return 0;
            case OCCLUSAL:   return 1;
            case DISTAL:     return 2;
            case MESIAL:     return 3;
            case LINGUAL:    return 4;
            case INCISAL:    return 5;
            case PALATAL:    return 6;
            default:         return 99;  // fallback para enums nuevos
        }
    }

    @Override
    public String toString() {
        return "DentalBenefit{" +
                "piece=" + (piece != null ? piece.getFdiCode() : "null") +
                ", surfaces=" + surfaces +
                ", benefitCode='" + benefitCode + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DentalBenefit that = (DentalBenefit) o;
        return Objects.equals(piece, that.piece) &&
                surfaces.equals(that.surfaces) &&
                benefitCode.equals(that.benefitCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(piece, surfaces, benefitCode);
    }
}