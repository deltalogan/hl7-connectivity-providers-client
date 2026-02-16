package com.hl7client.model.constants;

/**
 * Constantes relacionadas con el formato y límites HL7 para prestaciones.
 */
public final class Hl7Constants {

    // Límites de longitud para los campos paramX
    public static final int MAX_LENGTH_PER_PARAM = 255;
    public static final int MAX_PARAMS_MEDICINA = 3;
    public static final int MAX_LENGTH_MEDICINA = MAX_LENGTH_PER_PARAM * MAX_PARAMS_MEDICINA; // 765
    public static final int MAX_LENGTH_ODONTOLOGIA = MAX_LENGTH_PER_PARAM;                   // 255

    // Formatos y separadores
    public static final String SEGMENT_SEPARATOR = "|";
    public static final String FIELD_SEPARATOR = "*";
    public static final String COUNT_SEPARATOR = "^";

    // Valores fijos odontología
    public static final String DENTAL_TOTAL_COUNT = "1";
    public static final String DENTAL_ORIGIN = "P";
    public static final String DENTAL_ITEM_QUANTITY = "1";
    public static final String DENTAL_PREFIX = "O";

    private Hl7Constants() {
        // No instanciar
    }
}