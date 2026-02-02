package com.hl7client.controller;

import com.hl7client.model.dto.request.hl7.*;
import com.hl7client.model.dto.response.hl7.*;
import com.hl7client.model.result.*;
import com.hl7client.service.Hl7Service;

import java.util.Objects;

public class Hl7Controller {

    private final Hl7Service hl7Service;

    public Hl7Controller(Hl7Service hl7Service) {
        this.hl7Service = Objects.requireNonNull(hl7Service);
    }

    public Hl7Result<ElegibilidadResponse> consultarElegibilidad(
            ElegibilidadRequest request
    ) {
        if (request == null) {
            return errorRequestInvalido("ElegibilidadRequest");
        }
        return hl7Service.consultarElegibilidad(request);
    }

    public Hl7Result<RegistracionResponse> consultarRegistracion(
            RegistracionRequest request
    ) {
        if (request == null) {
            return errorRequestInvalido("RegistracionRequest");
        }
        return hl7Service.consultarRegistracion(request);
    }

    public Hl7Result<CancelacionResponse> consultarCancelacion(
            CancelacionRequest request
    ) {
        if (request == null) {
            return errorRequestInvalido("CancelacionRequest");
        }
        return hl7Service.cancelarPrestacion(request);
    }

    private <T> Hl7Result<T> errorRequestInvalido(String nombre) {
        return Hl7Result.error(
                Hl7Error.technical(
                        nombre + " no puede ser null",
                        Hl7ErrorOrigin.PARSEO
                )
        );
    }
}
