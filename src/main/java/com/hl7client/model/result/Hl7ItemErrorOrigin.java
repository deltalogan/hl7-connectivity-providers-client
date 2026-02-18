package com.hl7client.model.result;

public enum Hl7ItemErrorOrigin {

    /** Error en un ítem del detalle */
    DETALLE,

    /** Error en subdetalle (por ejemplo prácticas, insumos, etc.) */
    @SuppressWarnings({"unused"})
    SUBDETALLE
}
