package com.hl7client.controller;

import com.hl7client.client.AuthRefresher;
import com.hl7client.service.AuthService;

public class SessionController implements AuthRefresher {

    private final AuthService authService;

    public SessionController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void refreshAuth() {
        authService.refreshAuth();
    }
}
