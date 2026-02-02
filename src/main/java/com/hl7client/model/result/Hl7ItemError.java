package com.hl7client.model.result;

import java.util.Objects;

public final class Hl7ItemError {

    private final String code;
    private final String message;
    private final Hl7ItemErrorOrigin origin;

    public Hl7ItemError(
            String code,
            String message,
            Hl7ItemErrorOrigin origin
    ) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.origin = Objects.requireNonNull(origin);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Hl7ItemErrorOrigin getOrigin() {
        return origin;
    }
}

