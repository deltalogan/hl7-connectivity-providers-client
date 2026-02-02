package com.hl7client.config;

import com.hl7client.model.dto.request.auth.DeviceRequest;
import com.hl7client.model.dto.response.auth.Prestador;

record SessionState(
        String token,
        String tokenExp,
        Prestador prestador,
        Environment environment,
        DeviceRequest device
) {

    private static final SessionState EMPTY =
            new SessionState(null, null, null, null, null);

    static SessionState empty() {
        return EMPTY;
    }

    boolean isEmpty() {
        return token == null
                && tokenExp == null
                && prestador == null
                && environment == null
                && device == null;
    }

    boolean hasAuth() {
        return token != null && !token.isBlank();
    }

    boolean isInitialized() {
        return environment != null && device != null;
    }
}
