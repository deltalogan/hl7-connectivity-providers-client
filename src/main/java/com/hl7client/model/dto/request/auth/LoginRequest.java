package com.hl7client.model.dto.request.auth;

public class LoginRequest {

    private String apiKey;
    private String email;
    private String password;
    private DeviceRequest device;

    public LoginRequest(String apiKey, String email, String password, DeviceRequest device) {
        this.apiKey = apiKey;
        this.email = email;
        this.password = password;
        this.device = device;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public DeviceRequest getDevice() {
        return device;
    }
}
