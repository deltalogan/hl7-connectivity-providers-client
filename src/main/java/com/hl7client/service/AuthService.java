package com.hl7client.service;

import com.hl7client.client.ApiClient;
import com.hl7client.client.ApiResponse;
import com.hl7client.client.AuthRefresher;
import com.hl7client.config.Environment;
import com.hl7client.config.EnvironmentConfig;
import com.hl7client.config.SessionContext;
import com.hl7client.config.SessionRefreshManager;
import com.hl7client.model.dto.request.auth.DeviceRequest;
import com.hl7client.model.dto.request.auth.LoginRequest;
import com.hl7client.model.dto.response.auth.LoginResponse;
import com.hl7client.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AuthService implements AuthRefresher {

    private final ApiClient apiClient;

    public AuthService() {
        this.apiClient = new ApiClient(this);
    }

    public AuthService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    // ---------- LOGIN ----------

    public void login(
            String email,
            char[] password,
            String apiKey,
            Environment environment
    ) {
        Objects.requireNonNull(environment, "Environment requerido");

        String url = EnvironmentConfig.getAuthUrl(environment);

        DeviceRequest device = createDevice();

        LoginRequest request = new LoginRequest(
                apiKey,
                email,
                new String(password),
                device
        );

        String jsonRequest = JsonUtil.toJson(request);
        Map<String, String> headers = defaultJsonHeaders();

        ApiResponse response = apiClient.post(url, jsonRequest, headers);

        if (response.isHttpError()) {
            throw new RuntimeException(
                    "Error técnico durante login (HTTP "
                            + response.getStatusCode() + ")"
            );
        }

        if (response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException(
                    "Respuesta vacía del servicio de autenticación"
            );
        }

        LoginResponse loginResponse =
                JsonUtil.fromJson(response.getBody(), LoginResponse.class);

        if (loginResponse == null || loginResponse.getToken() == null) {
            throw new RuntimeException(
                    "Respuesta inválida del servicio de autenticación"
            );
        }

        initializeSession(loginResponse, environment, device);
    }

    // ---------- LOGOUT MANUAL ----------

    public void logout() {
        SessionRefreshManager.stop();
        SessionContext.clear();
    }

    // ---------- AUTH REFRESH ----------

    @Override
    public void refreshAuth() {
        doRefresh();
    }

    private void doRefresh() {

        if (!SessionContext.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay sesión activa para refrescar"
            );
        }

        if (SessionContext.getDevice() == null) {
            throw new IllegalStateException(
                    "No hay información de device en sesión"
            );
        }

        String url = EnvironmentConfig.getAuthRefreshUrl(
                SessionContext.getEnvironment()
        );

        String body = JsonUtil.toJson(SessionContext.getDevice());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + SessionContext.getToken());

        ApiResponse response = apiClient.post(url, body, headers);

        if (response.isHttpError()) {
            logout(); // 🔴 limpieza fuerte
            throw new RuntimeException(
                    "Error técnico en auth-refresh (HTTP "
                            + response.getStatusCode() + ")"
            );
        }

        if (response.getBody() == null || response.getBody().isBlank()) {
            logout();
            throw new RuntimeException(
                    "Respuesta vacía en auth-refresh"
            );
        }

        LoginResponse refreshResponse =
                JsonUtil.fromJson(response.getBody(), LoginResponse.class);

        if (refreshResponse == null || refreshResponse.getToken() == null) {
            logout();
            throw new RuntimeException(
                    "Respuesta inválida de auth-refresh"
            );
        }

        SessionContext.updateAuth(
                refreshResponse.getToken(),
                refreshResponse.getExp(),
                refreshResponse.getModelEspecifico()
        );

        SessionRefreshManager.ensureStarted(this);
    }

    // ---------- helpers ----------

    private DeviceRequest createDevice() {
        String deviceId = UUID.randomUUID().toString();
        return new DeviceRequest(deviceId, deviceId, "HL7-Java-Client");
    }

    private void initializeSession(
            LoginResponse response,
            Environment environment,
            DeviceRequest device
    ) {
        SessionContext.initialize(
                response.getToken(),
                response.getExp(),
                response.getModelEspecifico(),
                environment,
                device
        );

        SessionRefreshManager.ensureStarted(this);
    }

    private Map<String, String> defaultJsonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        return headers;
    }
}
