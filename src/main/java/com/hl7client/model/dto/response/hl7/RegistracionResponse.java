package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistracionResponse {

    private RegistracionCabecera cabecera;

    // En muchos casos viene null, pero lo dejamos preparado
    private RegistracionDetalle[] detalle;

    public RegistracionCabecera getCabecera() {
        return cabecera;
    }

    @SuppressWarnings({"unused"})
    public void setCabecera(RegistracionCabecera cabecera) {
        this.cabecera = cabecera;
    }

    public RegistracionDetalle[] getDetalle() {
        return detalle;
    }

    @SuppressWarnings({"unused"})
    public void setDetalle(RegistracionDetalle[] detalle) {
        this.detalle = detalle;
    }
}
