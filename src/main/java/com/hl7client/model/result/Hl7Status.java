package com.hl7client.model.result;

public enum Hl7Status {

    /** Operación exitosa */
    OK,

    /** Operación exitosa con observaciones */
    PARTIAL,

    /** Rechazo funcional (reglas de negocio) */
    REJECTED,

    /** Error técnico / sesión / infraestructura */
    ERROR
}
