package com.hl7client.ui.frames;

import com.hl7client.Application;
import com.hl7client.ApplicationCloseIntent;
import com.hl7client.controller.Hl7Controller;
import com.hl7client.ui.dialogs.CancelacionDialog;
import com.hl7client.ui.dialogs.ElegibilidadDialog;
import com.hl7client.ui.dialogs.RegistracionDialog;
import com.hl7client.ui.util.CloseAction;
import com.hl7client.ui.util.WindowCloseController;
import com.hl7client.ui.util.WindowSizer;
import com.hl7client.util.AppInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;

public class MainFrame extends JFrame {

    private final Hl7Controller hl7Controller;

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.90;
    private static final String IMAGE_PATH_ICON = "/icons/icon.png";

    public MainFrame(Application application, Hl7Controller hl7Controller) {
        this.hl7Controller = Objects.requireNonNull(hl7Controller);

        initComponents();
        configureFrame();
        initActions();
        initShortcuts();
        initCloseBehavior(application);

        // Foco inicial
        SwingUtilities.invokeLater(() ->
                eligibilityButton.requestFocusInWindow()
        );
    }

    // -------------------------------------------------
    // Public API
    // -------------------------------------------------

    public void configureTitle(String prestador, String environment) {
        setTitle(AppInfo.mainTitle(prestador, environment));
    }

    // -------------------------------------------------
    // Frame configuration
    // -------------------------------------------------

    private void configureFrame() {
        Image icon = loadImageResource(IMAGE_PATH_ICON);
        if (icon != null) {
            setIconImage(icon);
        }

        pack();

        // Establecemos tamaño mínimo proporcional a la pantalla
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);  // ≈ 22% → ajustable

        // Aplicamos tamaño inicial deseado
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        setLocationRelativeTo(null);
    }

    private Image loadImageResource(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println(
                    MessageFormat.format(
                            "Resource not found: {0}",
                            resourcePath
                    )
            );
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // -------------------------------------------------
    // Actions
    // -------------------------------------------------

    private void initActions() {

        eligibilityButton.addActionListener(e ->
                openDialog(new ElegibilidadDialog(
                        this,
                        hl7Controller,
                        eligibilityButton.getText()
                ))
        );

        registrationButton.addActionListener(e ->
                openDialog(new RegistracionDialog(
                        this,
                        hl7Controller,
                        registrationButton.getText()
                ))
        );

        cancellationButton.addActionListener(e ->
                openDialog(new CancelacionDialog(
                        this,
                        hl7Controller,
                        cancellationButton.getText()
                ))
        );
    }

    private void initShortcuts() {
        eligibilityButton.setMnemonic(KeyEvent.VK_E);     // ALT+E
        registrationButton.setMnemonic(KeyEvent.VK_R);    // ALT+R
        cancellationButton.setMnemonic(KeyEvent.VK_C);    // ALT+C
        // Logout: mnemonic vive en el Action
    }

    private void initCloseBehavior(Application application) {

        CloseAction logoutAction =
                new CloseAction(
                        "Logout",
                        "Cerrar sesión",
                        application,
                        ApplicationCloseIntent.LOGOUT_SESSION
                );

        // ALT + L
        logoutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);

        // Botón Logout
        logoutButton.setAction(logoutAction);

        // ENTER → Logout
        getRootPane().setDefaultButton(logoutButton);

        // ESC → Logout
        getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "logout");

        getRootPane()
                .getActionMap()
                .put("logout", logoutAction);

        // Click en ❌ ventana
        WindowCloseController.install(
                this,
                application,
                ApplicationCloseIntent.LOGOUT_SESSION
        );
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private void openDialog(JDialog dialog) {
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - meagan.carter169@mazun.org
        logScrollPane = new JScrollPane();
        logTable = new JTable();
        eligibilityButton = new JButton();
        registrationButton = new JButton();
        cancellationButton = new JButton();
        logoutButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

        //======== logScrollPane ========
        {
            logScrollPane.setViewportView(logTable);
        }
        contentPane.add(logScrollPane, new GridBagConstraints(0, 0, 1, 4, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- eligibilityButton ----
        eligibilityButton.setText("Eligibility");
        contentPane.add(eligibilityButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- registrationButton ----
        registrationButton.setText("Registration");
        contentPane.add(registrationButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- cancellationButton ----
        cancellationButton.setText("Cancellation");
        contentPane.add(cancellationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- logoutButton ----
        logoutButton.setText("Logout");
        contentPane.add(logoutButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - meagan.carter169@mazun.org
    private JScrollPane logScrollPane;
    private JTable logTable;
    private JButton eligibilityButton;
    private JButton registrationButton;
    private JButton cancellationButton;
    private JButton logoutButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
