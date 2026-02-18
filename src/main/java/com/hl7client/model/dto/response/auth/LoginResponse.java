package com.hl7client.model.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    private String token;
    private String exp;
    private Prestador modelEspecifico;

    public LoginResponse() {
    }

    public String getToken() {
        return token;
    }

    public String getExp() {
        return exp;
    }

    public Prestador getModelEspecifico() {
        return modelEspecifico;
    }

    @SuppressWarnings({"unused"})
    public void setToken(String token) {
        this.token = token;
    }

    @SuppressWarnings({"unused"})
    public void setExp(String exp) {
        this.exp = exp;
    }

    @SuppressWarnings({"unused"})
    public void setModelEspecifico(Prestador modelEspecifico) {
        this.modelEspecifico = modelEspecifico;
    }
}
