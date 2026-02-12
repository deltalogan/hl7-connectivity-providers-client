package com.hl7client.model.dental;

import java.util.List;

/**
 * Catálogo estático e inmutable de todas las piezas dentales según notación FDI (ISO 3950),
 * incluyendo dientes permanentes (11–48) y temporales/deciduos (51–85).
 * <p>
 * Cada entrada contiene:
 * <ul>
 *   <li>FDI numérico</li>
 *   <li>Descripción anatómica clara</li>
 *   <li>Tipo de diente (para validaciones de superficies)</li>
 * </ul>
 */
public final class DentalToothData {

    /**
     * Entrada individual de una pieza dental.
     */
    public static class ToothEntry {
        public final int fdi;
        public final String description;
        public final DentalPieceType type;

        public ToothEntry(int fdi, String description, DentalPieceType type) {
            this.fdi = fdi;
            this.description = description;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%d - %s (%s)", fdi, description, type);
        }
    }

    // Dientes permanentes (adultos)
    private static final List<ToothEntry> PERMANENT_TEETH = List.of(
            // Cuadrante superior derecho
            new ToothEntry(11, "Incisivo central superior derecho", DentalPieceType.INCISIVO),
            new ToothEntry(12, "Incisivo lateral superior derecho", DentalPieceType.INCISIVO),
            new ToothEntry(13, "Canino superior derecho", DentalPieceType.CANINO),
            new ToothEntry(14, "Primer premolar superior derecho", DentalPieceType.PREMOLAR),
            new ToothEntry(15, "Segundo premolar superior derecho", DentalPieceType.PREMOLAR),
            new ToothEntry(16, "Primer molar superior derecho", DentalPieceType.MOLAR),
            new ToothEntry(17, "Segundo molar superior derecho", DentalPieceType.MOLAR),
            new ToothEntry(18, "Tercer molar superior derecho (muela del juicio)", DentalPieceType.MOLAR),

            // Cuadrante superior izquierdo
            new ToothEntry(21, "Incisivo central superior izquierdo", DentalPieceType.INCISIVO),
            new ToothEntry(22, "Incisivo lateral superior izquierdo", DentalPieceType.INCISIVO),
            new ToothEntry(23, "Canino superior izquierdo", DentalPieceType.CANINO),
            new ToothEntry(24, "Primer premolar superior izquierdo", DentalPieceType.PREMOLAR),
            new ToothEntry(25, "Segundo premolar superior izquierdo", DentalPieceType.PREMOLAR),
            new ToothEntry(26, "Primer molar superior izquierdo", DentalPieceType.MOLAR),
            new ToothEntry(27, "Segundo molar superior izquierdo", DentalPieceType.MOLAR),
            new ToothEntry(28, "Tercer molar superior izquierdo (muela del juicio)", DentalPieceType.MOLAR),

            // Cuadrante inferior izquierdo
            new ToothEntry(31, "Incisivo central inferior izquierdo", DentalPieceType.INCISIVO),
            new ToothEntry(32, "Incisivo lateral inferior izquierdo", DentalPieceType.INCISIVO),
            new ToothEntry(33, "Canino inferior izquierdo", DentalPieceType.CANINO),
            new ToothEntry(34, "Primer premolar inferior izquierdo", DentalPieceType.PREMOLAR),
            new ToothEntry(35, "Segundo premolar inferior izquierdo", DentalPieceType.PREMOLAR),
            new ToothEntry(36, "Primer molar inferior izquierdo", DentalPieceType.MOLAR),
            new ToothEntry(37, "Segundo molar inferior izquierdo", DentalPieceType.MOLAR),
            new ToothEntry(38, "Tercer molar inferior izquierdo (muela del juicio)", DentalPieceType.MOLAR),

            // Cuadrante inferior derecho
            new ToothEntry(41, "Incisivo central inferior derecho", DentalPieceType.INCISIVO),
            new ToothEntry(42, "Incisivo lateral inferior derecho", DentalPieceType.INCISIVO),
            new ToothEntry(43, "Canino inferior derecho", DentalPieceType.CANINO),
            new ToothEntry(44, "Primer premolar inferior derecho", DentalPieceType.PREMOLAR),
            new ToothEntry(45, "Segundo premolar inferior derecho", DentalPieceType.PREMOLAR),
            new ToothEntry(46, "Primer molar inferior derecho", DentalPieceType.MOLAR),
            new ToothEntry(47, "Segundo molar inferior derecho", DentalPieceType.MOLAR),
            new ToothEntry(48, "Tercer molar inferior derecho (muela del juicio)", DentalPieceType.MOLAR)
    );

    // Dientes temporales / deciduos (niños)
    private static final List<ToothEntry> TEMPORARY_TEETH = List.of(
            // Cuadrante superior derecho
            new ToothEntry(51, "Incisivo central superior derecho temporal", DentalPieceType.INCISIVO),
            new ToothEntry(52, "Incisivo lateral superior derecho temporal", DentalPieceType.INCISIVO),
            new ToothEntry(53, "Canino superior derecho temporal", DentalPieceType.CANINO),
            new ToothEntry(54, "Primer molar superior derecho temporal", DentalPieceType.MOLAR),
            new ToothEntry(55, "Segundo molar superior derecho temporal", DentalPieceType.MOLAR),

            // Cuadrante superior izquierdo
            new ToothEntry(61, "Incisivo central superior izquierdo temporal", DentalPieceType.INCISIVO),
            new ToothEntry(62, "Incisivo lateral superior izquierdo temporal", DentalPieceType.INCISIVO),
            new ToothEntry(63, "Canino superior izquierdo temporal", DentalPieceType.CANINO),
            new ToothEntry(64, "Primer molar superior izquierdo temporal", DentalPieceType.MOLAR),
            new ToothEntry(65, "Segundo molar superior izquierdo temporal", DentalPieceType.MOLAR),

            // Cuadrante inferior izquierdo
            new ToothEntry(71, "Incisivo central inferior izquierdo temporal", DentalPieceType.INCISIVO),
            new ToothEntry(72, "Incisivo lateral inferior izquierdo temporal", DentalPieceType.INCISIVO),
            new ToothEntry(73, "Canino inferior izquierdo temporal", DentalPieceType.CANINO),
            new ToothEntry(74, "Primer molar inferior izquierdo temporal", DentalPieceType.MOLAR),
            new ToothEntry(75, "Segundo molar inferior izquierdo temporal", DentalPieceType.MOLAR),

            // Cuadrante inferior derecho
            new ToothEntry(81, "Incisivo central inferior derecho temporal", DentalPieceType.INCISIVO),
            new ToothEntry(82, "Incisivo lateral inferior derecho temporal", DentalPieceType.INCISIVO),
            new ToothEntry(83, "Canino inferior derecho temporal", DentalPieceType.CANINO),
            new ToothEntry(84, "Primer molar inferior derecho temporal", DentalPieceType.MOLAR),
            new ToothEntry(85, "Segundo molar inferior derecho temporal", DentalPieceType.MOLAR)
    );

    /**
     * Obtiene la lista de piezas dentales según el tipo de dentición.
     *
     * @param child true para dentición temporal (niños), false para permanente (adultos)
     * @return lista inmutable de ToothEntry
     */
    public static List<ToothEntry> getTeeth(boolean child) {
        return child ? TEMPORARY_TEETH : PERMANENT_TEETH;
    }

    /**
     * Busca una pieza dental por su código FDI.
     *
     * @param fdi código FDI (ej: 11, 36, 63, 84)
     * @return ToothEntry si existe, o null si no se encuentra
     */
    public static ToothEntry findByFdi(int fdi) {
        for (ToothEntry entry : PERMANENT_TEETH) {
            if (entry.fdi == fdi) return entry;
        }
        for (ToothEntry entry : TEMPORARY_TEETH) {
            if (entry.fdi == fdi) return entry;
        }
        return null;
    }

    private DentalToothData() {
        // Clase utilitaria estática → no instanciable
    }
}