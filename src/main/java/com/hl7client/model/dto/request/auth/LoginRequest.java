package com.hl7client.model.dto.request.auth;

public class LoginRequest {

    private final String apiKey;
    private final String email;
    private final String password;
    private final DeviceRequest device;

    public LoginRequest(String apiKey, String email, String password, DeviceRequest device) {
        this.apiKey = apiKey;
        this.email = email;
        this.password = password;
        this.device = device;
    }

    @SuppressWarnings("unused")
    public String getApiKey() {
        return apiKey;
    }

    @SuppressWarnings("unused")
    public String getEmail() {
        return email;
    }

    @SuppressWarnings("unused")
    public String getPassword() {
        return password;
    }

    @SuppressWarnings("unused")
    public DeviceRequest getDevice() {
        return device;
    }
}
