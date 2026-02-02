package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModoRegistracion {

    NORMAL("N"),
    URGENCIA("U");

    private final String value;

    ModoRegistracion(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
