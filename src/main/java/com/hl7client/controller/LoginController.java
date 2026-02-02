package com.hl7client.controller;

import com.hl7client.client.AuthExpiredException;
import com.hl7client.client.AuthRefresher;
import com.hl7client.config.Environment;
import com.hl7client.config.SessionEndReason;
import com.hl7client.model.result.Hl7Error;
import com.hl7client.model.result.Hl7ErrorOrigin;
import com.hl7client.model.result.Hl7Result;
import com.hl7client.service.AuthService;
import com.hl7client.ui.frames.LoginFrame;

import java.util.Arrays;
import java.util.Objects;

public class LoginController implements AuthRefresher {

    private final LoginFrame view;
    private final AuthService authService;
    private LoginListener loginListener;

    public LoginController(
            LoginFrame view,
            AuthService authService
    ) {
        this.view = Objects.requireNonNull(view);
        this.authService = Objects.requireNonNull(authService);
        this.view.setController(this);
    }

    // -------------------------------------------------
    // Listener
    // -------------------------------------------------

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = Objects.requireNonNull(loginListener);
    }

    // -------------------------------------------------
    // LOGIN
    // -------------------------------------------------

    public Hl7Result<Void> login(
            String email,
            char[] password,
            String apiKey,
            String environment
    ) {
        try {
            Environment env = Environment.valueOf(environment);

            authService.login(email, password, apiKey, env);

            clearPassword(password);

            if (loginListener != null) {
                loginListener.onLoginSuccess();
            }

            return Hl7Result.ok(null);

        } catch (IllegalArgumentException e) {
            return Hl7Result.rejected(
                    null,
                    Hl7Error.functional(
                            "INVALID_ENVIRONMENT",
                            "Environment inválido: " + environment
                    )
            );

        } catch (Exception e) {
            return Hl7Result.error(
                    Hl7Error.technical(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Error técnico durante login",
                            Hl7ErrorOrigin.TRANSPORTE
                    )
            );
        }
    }

    // -------------------------------------------------
    // CANCEL
    // -------------------------------------------------

    public void cancel() {
        if (loginListener != null) {
            loginListener.onSessionEnded(SessionEndReason.MANUAL_LOGOUT);
        }
    }

    // -------------------------------------------------
    // AUTH REFRESH
    // -------------------------------------------------

    @Override
    public void refreshAuth() {
        try {
            authService.refreshAuth();

        } catch (AuthExpiredException e) {
            // 401 real → token inválido / revocado
            notifySessionEnd(SessionEndReason.UNAUTHORIZED);

        } catch (Exception e) {
            // refresh fallido → expiración normal
            notifySessionEnd(SessionEndReason.SESSION_EXPIRED);
        }
    }

    private void notifySessionEnd(SessionEndReason reason) {
        if (loginListener != null) {
            loginListener.onSessionEnded(reason);
        }
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private void clearPassword(char[] password) {
        Arrays.fill(password, '\0');
    }

    // -------------------------------------------------
    // Callback
    // -------------------------------------------------

    public interface LoginListener {
        void onLoginSuccess();
        void onSessionEnded(SessionEndReason reason);
    }
}
