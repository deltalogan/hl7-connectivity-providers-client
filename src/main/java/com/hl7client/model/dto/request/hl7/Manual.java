package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Manual {

    MANUAL("0"),
    CAPITADOR("C"),
    COMSULTA("L");

    private final String value;

    Manual(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
