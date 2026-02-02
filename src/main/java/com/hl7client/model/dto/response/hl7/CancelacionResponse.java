package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelacionResponse {

    private CancelacionCabecera cabecera;
    private CancelacionDetalle[] detalle;

    public CancelacionCabecera getCabecera() {
        return cabecera;
    }

    public void setCabecera(CancelacionCabecera cabecera) {
        this.cabecera = cabecera;
    }

    public CancelacionDetalle[] getDetalle() {
        return detalle;
    }

    public void setDetalle(CancelacionDetalle[] detalle) {
        this.detalle = detalle;
    }
}
