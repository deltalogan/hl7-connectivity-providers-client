package com.hl7client;

import com.hl7client.client.ApiClient;
import com.hl7client.config.SessionContext;
import com.hl7client.config.SessionEndReason;
import com.hl7client.controller.Hl7Controller;
import com.hl7client.controller.LoginController;
import com.hl7client.service.AuthService;
import com.hl7client.service.Hl7Service;
import com.hl7client.ui.frames.LoginFrame;
import com.hl7client.ui.frames.MainFrame;
import com.hl7client.ui.theme.ThemeManager;

import javax.swing.*;

public class Application {

    private LoginFrame loginFrame;
    private MainFrame mainFrame;
    private AuthService authService;

    // -------------------------------------------------
    // Entry point
    // -------------------------------------------------

    public void start() {
        ThemeManager.getInstance().initialize();
        openLogin();
    }

    // -------------------------------------------------
    // Cierre centralizado
    // -------------------------------------------------

    public void requestClose(ApplicationCloseIntent intent) {
        switch (intent) {

            case EXIT_APPLICATION -> {
                int option = JOptionPane.showConfirmDialog(
                        null,
                        "¿Está seguro que desea cerrar la aplicación?",
                        "Salir",
                        JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    closeLoginFrame();
                    closeMainFrame();
                    System.exit(0);
                }
            }

            case LOGOUT_SESSION -> {
                int option = JOptionPane.showConfirmDialog(
                        null,
                        "¿Está seguro que desea cerrar sesión?",
                        "Cerrar sesión",
                        JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    logoutManually();
                }
            }
        }
    }

    // -------------------------------------------------
    // Logout / sesión
    // -------------------------------------------------

    private void logoutManually() {
        authService.logout();
        handleSessionEnd(SessionEndReason.MANUAL_LOGOUT);
    }

    public void forceLogout(SessionEndReason reason) {
        handleSessionEnd(reason);
    }

    // -------------------------------------------------
    // Login
    // -------------------------------------------------

    private void openLogin() {
        closeMainFrame();

        authService = new AuthService();
        loginFrame = new LoginFrame(this);

        LoginController controller =
                new LoginController(loginFrame, authService);

        controller.setLoginListener(new LoginController.LoginListener() {
            @Override
            public void onLoginSuccess() {
                openMainFrame();
            }

            @Override
            public void onSessionEnded(SessionEndReason reason) {
                handleSessionEnd(reason);
            }
        });

        loginFrame.setVisible(true);
    }

    // -------------------------------------------------
    // Main
    // -------------------------------------------------

    private void openMainFrame() {
        closeLoginFrame();

        ApiClient apiClient = new ApiClient(authService);
        Hl7Service hl7Service = new Hl7Service(apiClient);
        Hl7Controller hl7Controller = new Hl7Controller(hl7Service);

        mainFrame = new MainFrame(this, hl7Controller);
        mainFrame.configureTitle(
                SessionContext.getPrestador().getRazonSocialPrestador(),
                SessionContext.getEnvironment().name()
        );
        mainFrame.setVisible(true);
    }

    // -------------------------------------------------
    // Session handling
    // -------------------------------------------------

    private void handleSessionEnd(SessionEndReason reason) {
        closeMainFrame();

        switch (reason) {
            case MANUAL_LOGOUT -> JOptionPane.showMessageDialog(null, "Sesión cerrada correctamente");
            case SESSION_EXPIRED -> JOptionPane.showMessageDialog(null, "La sesión expiró");
            case UNAUTHORIZED -> JOptionPane.showMessageDialog(null, "No autorizado");
        }

        openLogin();
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private void closeLoginFrame() {
        if (loginFrame != null) {
            loginFrame.dispose();
            loginFrame = null;
        }
    }

    private void closeMainFrame() {
        if (mainFrame != null) {
            mainFrame.dispose();
            mainFrame = null;
        }
    }
}
