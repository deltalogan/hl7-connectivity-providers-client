package com.hl7client.config;

import com.hl7client.model.dto.request.auth.DeviceRequest;
import com.hl7client.model.dto.response.auth.Prestador;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class SessionContext {

    private static final Logger LOGGER = Logger.getLogger(SessionContext.class.getName());

    private static final AtomicReference<SessionState> STATE =
            new AtomicReference<>(SessionState.empty());

    private SessionContext() {
    }

    // ---------- TOKEN ----------

    public static String getToken() {
        return STATE.get().token();
    }

    public static boolean isAuthenticated() {
        SessionState state = STATE.get();
        return state.token() != null && !state.token().isEmpty();
    }

    // ---------- TOKEN EXP ----------

    public static String getTokenExp() {
        return STATE.get().tokenExp();
    }

    // ---------- PRESTADOR ----------

    public static Prestador getPrestador() {
        return STATE.get().prestador();
    }

    // ---------- ENVIRONMENT ----------

    public static Environment getEnvironment() {
        return STATE.get().environment();
    }

    // ---------- DEVICE ----------

    public static DeviceRequest getDevice() {
        return STATE.get().device();
    }

    // ---------- SETTERS ATÓMICOS ----------

    public static void initialize(
            String token,
            String tokenExp,
            Prestador prestador,
            Environment environment,
            DeviceRequest device
    ) {
        STATE.set(new SessionState(
                token,
                tokenExp,
                prestador,
                environment,
                device
        ));

        LOGGER.info("Session initialized. Environment=" + environment);
    }

    public static void updateAuth(
            String token,
            String tokenExp,
            Prestador prestador
    ) {
        STATE.updateAndGet(current -> {
            if (current.environment() == null || current.device() == null) {
                LOGGER.warning("Updating auth on uninitialized session state");
            }

            return new SessionState(
                    token,
                    tokenExp,
                    prestador,
                    current.environment(),
                    current.device()
            );
        });

        LOGGER.fine("Session authentication updated");
    }

    // ---------- LIMPIEZA ----------

    public static void clear() {
        STATE.set(SessionState.empty());
        LOGGER.info("Session cleared");
    }
}
