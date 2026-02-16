package com.hl7client.config;

import com.hl7client.model.dto.request.auth.DeviceRequest;
import com.hl7client.model.dto.response.auth.Prestador;

public final class SessionState {

    private final String token;
    private final String tokenExp;
    private final Prestador prestador;
    private final Environment environment;
    private final DeviceRequest device;

    // <-- CAMBIO 1: Quitamos "private" para que sea package-private
    SessionState(
            String token,
            String tokenExp,
            Prestador prestador,
            Environment environment,
            DeviceRequest device
    ) {
        this.token = token;
        this.tokenExp = tokenExp;
        this.prestador = prestador;
        this.environment = environment;
        this.device = device;
    }

    // Instancia vacía (singleton-like)
    private static final SessionState EMPTY = new SessionState(null, null, null, null, null);

    public static SessionState empty() {
        return EMPTY;
    }

    // Getters
    public String token() {
        return token;
    }

    public String tokenExp() {
        return tokenExp;
    }

    public Prestador prestador() {
        return prestador;
    }

    public Environment environment() {
        return environment;
    }

    public DeviceRequest device() {
        return device;
    }

    // Métodos de utilidad
    public boolean isEmpty() {
        return token == null
                && tokenExp == null
                && prestador == null
                && environment == null
                && device == null;
    }

    @SuppressWarnings("unused")
    public boolean hasAuth() {
        return token != null && !token.trim().isEmpty();
    }

    @SuppressWarnings("unused")
    public boolean isInitialized() {
        return environment != null && device != null;
    }

    // toString para debugging
    @Override
    public String toString() {
        return "SessionState{" +
                "token='" + token + '\'' +
                ", prestador=" + prestador +
                ", environment=" + environment +
                ", device=" + device +
                '}';
    }
}