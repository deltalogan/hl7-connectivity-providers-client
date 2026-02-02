package com.hl7client.model.result;

import java.util.Objects;

public final class Hl7Error {

    private final String code;
    private final String message;
    private final Hl7ErrorOrigin origin;
    private final boolean session;

    private Hl7Error(
            String code,
            String message,
            Hl7ErrorOrigin origin,
            boolean session
    ) {
        this.code = code;
        this.message = Objects.requireNonNull(message);
        this.origin = Objects.requireNonNull(origin);
        this.session = session;
    }

    // ================== FACTORIES ==================

    /** Error funcional (rechazo de negocio) */
    public static Hl7Error functional(
            String code,
            String message
    ) {
        return new Hl7Error(
                code,
                message,
                Hl7ErrorOrigin.CABECERA,
                false
        );
    }

    /** Error técnico (infraestructura, transporte, parseo, etc.) */
    public static Hl7Error technical(
            String message,
            Hl7ErrorOrigin origin
    ) {
        return new Hl7Error(
                null,
                message,
                origin,
                false
        );
    }

    /** Sesión expirada */
    public static Hl7Error sessionExpired() {
        return new Hl7Error(
                "SESSION_EXPIRED",
                "La sesión ha expirado",
                Hl7ErrorOrigin.TRANSPORTE,
                true
        );
    }

    // ================== HELPERS ==================

    public boolean isSession() {
        return session;
    }

    // ================== GETTERS ==================

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Hl7ErrorOrigin getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "Hl7Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", origin=" + origin +
                ", session=" + session +
                '}';
    }
}
