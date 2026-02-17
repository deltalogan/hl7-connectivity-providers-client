package com.hl7client.ui.frames;

import com.hl7client.Application;
import com.hl7client.ApplicationCloseIntent;
import com.hl7client.config.Environment;
import com.hl7client.controller.LoginController;
import com.hl7client.model.result.Hl7Error;
import com.hl7client.model.result.Hl7Result;
import com.hl7client.ui.images.ScalableImageLabel;
import com.hl7client.ui.theme.ThemeButtonBinder;
import com.hl7client.ui.util.AcceptAction;
import com.hl7client.ui.util.CloseAction;
import com.hl7client.ui.util.Hl7UiErrorHandler;
import com.hl7client.ui.util.WindowCloseController;
import com.hl7client.ui.util.WindowSizer;
import com.hl7client.util.AppInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;

public class LoginFrame extends JFrame {

    private LoginController controller;
    private final CloseAction closeAction;

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.50;

    private static final String IMAGE_PATH_LOGO  = "/icons/logo.png";
    private static final String IMAGE_PATH_ICON  = "/icons/icon.png";
    private static final String SPLASH_PATH      = "/icons/splash.gif";

    public LoginFrame(Application application) {
        initComponents();

        ThemeButtonBinder.bind(themeButton);

        configureFrame();
        loadEnvironmentComboBox();

        closeAction = new CloseAction(
                application,
                ApplicationCloseIntent.EXIT_APPLICATION
        );

        initActions();
        initShortcuts();
        initWindowClose(application);
    }

    // =========================================================
    // Controller
    // =========================================================

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    // =========================================================
    // Actions
    // =========================================================

    private void initActions() {

        Action acceptAction = new AcceptAction<>(
                "Accept",
                this,
                getClass().getResource(SPLASH_PATH),
                this::doLogin,
                this::onLoginResult
        );

        acceptButton.setAction(acceptAction);
        cancelButton.setAction(closeAction);

        // ENTER → Accept
        getRootPane().setDefaultButton(acceptButton);
    }

    private void initShortcuts() {
        acceptButton.setMnemonic(KeyEvent.VK_A);   // ALT + A
        cancelButton.setMnemonic(KeyEvent.VK_C);   // ALT + C
    }

    private void initWindowClose(Application application) {

        getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");

        getRootPane()
                .getActionMap()
                .put("close", closeAction);

        WindowCloseController.install(
                this,
                application,
                ApplicationCloseIntent.EXIT_APPLICATION
        );
    }

    // =========================================================
    // Accept workflow
    // =========================================================

    private Hl7Result<Void> doLogin() {
        if (getEmail().isEmpty()
                || getPassword().length == 0
                || getApiKey().isEmpty()) {

            return Hl7Result.rejected(
                    null,
                    Hl7Error.functional(
                            null,
                            "Email, password y API Key son obligatorios"
                    )
            );
        }

        return controller.login(
                getEmail(),
                getPassword(),
                getApiKey(),
                getEnvironment()
        );
    }

    private void onLoginResult(Hl7Result<Void> result) {

        Hl7UiErrorHandler.mostrarResultado(
                this,
                result,
                "login",
                null
        );

        if (result != null && result.isOk()) {
            dispose();
        }
    }

    // =========================================================
    // UI configuration
    // =========================================================

    private void configureFrame() {
        setTitle(AppInfo.loginTitle());

        Image icon = loadIcon();
        if (icon != null) {
            setIconImage(icon);
        }

        pack();

        // Establecemos tamaño mínimo proporcional a la pantalla
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);

        // Aplicamos tamaño inicial deseado
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        setLocationRelativeTo(null);

        ScalableImageLabel scalable = new ScalableImageLabel();
        scalable.setHorizontalAlignment(SwingConstants.CENTER);
        scalable.setImage(getClass().getResource(IMAGE_PATH_LOGO));
        loginLabel.setLayout(new BorderLayout());
        loginLabel.add(scalable, BorderLayout.CENTER);

        // Foco inicial
        SwingUtilities.invokeLater(() ->
                emailTextField.requestFocusInWindow()
        );
    }

    private void loadEnvironmentComboBox() {
        environmentComboBox.removeAllItems();
        for (Environment env : Environment.values()) {
            environmentComboBox.addItem(env.name());
        }
    }

    // =========================================================
    // Getters
    // =========================================================

    public String getEmail() {
        return emailTextField.getText();
    }

    public char[] getPassword() {
        return passwordPasswordField.getPassword();
    }

    public String getApiKey() {
        return apiKeyTextField.getText();
    }

    public String getEnvironment() {
        return environmentComboBox.getSelectedItem() != null
                ? environmentComboBox.getSelectedItem().toString()
                : null;
    }

    // =========================================================
    // Resources
    // =========================================================

    private Image loadIcon() {
        URL url = getClass().getResource(IMAGE_PATH_ICON);
        if (url == null) {
            System.err.println("Resource not found: " + IMAGE_PATH_ICON);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        languageComboBox = new JComboBox<>();
        themeButton = new JButton();
        loginLabel = new JLabel();
        emailLabel = new JLabel();
        emailTextField = new JTextField();
        passwordLabel = new JLabel();
        passwordPasswordField = new JPasswordField();
        apiKeyLabel = new JLabel();
        apiKeyTextField = new JTextField();
        environmentLabel = new JLabel();
        environmentComboBox = new JComboBox<>();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- languageComboBox ----
        languageComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
            "String"
        }));
        contentPane.add(languageComboBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(themeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 0), 0, 0));
        contentPane.add(loginLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- emailLabel ----
        emailLabel.setText("Email:");
        contentPane.add(emailLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(emailTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- passwordLabel ----
        passwordLabel.setText("Password:");
        contentPane.add(passwordLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(passwordPasswordField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- apiKeyLabel ----
        apiKeyLabel.setText("API Key:");
        contentPane.add(apiKeyLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(apiKeyTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- environmentLabel ----
        environmentLabel.setText("Environment:");
        contentPane.add(environmentLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- environmentComboBox ----
        environmentComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
            "String"
        }));
        contentPane.add(environmentComboBox, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JComboBox<String> languageComboBox;
    private JButton themeButton;
    private JLabel loginLabel;
    private JLabel emailLabel;
    private JTextField emailTextField;
    private JLabel passwordLabel;
    private JPasswordField passwordPasswordField;
    private JLabel apiKeyLabel;
    private JTextField apiKeyTextField;
    private JLabel environmentLabel;
    private JComboBox<String> environmentComboBox;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}