package com.hl7client.model.enums;

public enum TipoMensaje {

    ODONTOLOGIA("D", "Odontología"),
    MEDICINA("O", "Medicina");

    private final String codigoHl7;
    private final String descripcion;

    TipoMensaje(String codigoHl7, String descripcion) {
        this.codigoHl7 = codigoHl7;
        this.descripcion = descripcion;
    }

    public String getCodigoHl7() {
        return codigoHl7;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
