package com.hl7client.ui.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.hl7client.controller.Hl7Controller;
import com.hl7client.model.dto.request.hl7.CancelacionRequest;
import com.hl7client.model.dto.request.hl7.Manual;
import com.hl7client.model.dto.response.hl7.CancelacionCabecera;
import com.hl7client.model.dto.response.hl7.CancelacionResponse;
import com.hl7client.model.result.Hl7Result;
import com.hl7client.ui.util.AcceptAction;
import com.hl7client.ui.util.DialogUtils;
import com.hl7client.ui.util.Hl7UiErrorHandler;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CancelacionDialog extends JDialog {

    private final Hl7Controller hl7Controller;

    private static final double SCREEN_RATIO = 0.75;
    private static final String SPLASH_PATH = "/icons/splash.gif";

    private static final DateTimeFormatter HL7_DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;

    // =========================
    // DatePicker
    // =========================
    private DatePicker altaDatePicker;

    public CancelacionDialog(
            Window owner,
            Hl7Controller hl7Controller,
            String titulo
    ) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.hl7Controller = Objects.requireNonNull(hl7Controller);
        setTitle(Objects.requireNonNullElse(titulo, "Cancelación"));

        initComponents();
        initDatePicker();
        initActions();
        initShortcuts();
        installCloseBehavior();

        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        // Valores FIJOS obligatorios - se asignan después de initComponents()
        modoTextField.setText("N");
        modoTextField.setEnabled(false);

        tipoTextField.setText("90");
        tipoTextField.setEnabled(false);

        // Foco inicial (modo está deshabilitado, pasamos al siguiente campo editable)
        SwingUtilities.invokeLater(() ->
                credenTextField.requestFocusInWindow()
        );
    }

    // =========================================================
    // DatePicker
    // =========================================================
    private void initDatePicker() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("yyyyMMdd");
        settings.setAllowKeyboardEditing(false);

        altaDatePicker = new DatePicker(settings);

        altaPanel.setLayout(new BorderLayout());
        altaPanel.add(altaDatePicker, BorderLayout.CENTER);
    }

    // =========================================================
    // Actions
    // =========================================================
    private void initActions() {
        Action acceptAction = new AcceptAction<>(
                "Accept",
                this,
                getClass().getResource(SPLASH_PATH),
                this::doCancelacion,
                this::onCancelacionResult
        );

        acceptButton.setAction(acceptAction);

        // ENTER → Accept
        getRootPane().setDefaultButton(acceptButton);
    }

    private void initShortcuts() {
        acceptButton.setMnemonic(KeyEvent.VK_A);   // ALT + A
        cancelButton.setMnemonic(KeyEvent.VK_C);   // ALT + C
    }

    // =========================================================
    // Accept workflow
    // =========================================================
    private Hl7Result<CancelacionResponse> doCancelacion() {
        return hl7Controller.consultarCancelacion(buildRequest());
    }

    private void onCancelacionResult(Hl7Result<CancelacionResponse> result) {
        if (result == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error técnico inesperado",
                    getTitle(),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String transac = result.getData()
                .map(CancelacionResponse::getCabecera)
                .map(CancelacionCabecera::getTransac)
                .map(String::valueOf)
                .orElse(null);

        Hl7UiErrorHandler.mostrarResultado(
                this,
                result,
                getTitle(),
                transac
        );

        if (result.isOk()) {
            result.getData().ifPresent(this::mostrarResultado);
            dispose();
        }
    }

    // =========================================================
    // Build Request
    // =========================================================
    private CancelacionRequest buildRequest() {
        CancelacionRequest request = new CancelacionRequest();

        // Valores FIJOS (leídos de campos deshabilitados)
        request.setModo(textValue(modoTextField));  // "N"
        request.setTipo(intValue(tipoTextField));   // 90

        request.setCreden(longValue(credenTextField));
        request.setAlta(toHl7(altaDatePicker.getDate()));
        request.setManual(manualValue(manualTextField));

        request.setTicketExt(
                ticketExtTextField.getText().isBlank()
                        ? 0
                        : intValue(ticketExtTextField)
        );

        request.setCancelCab(intValue(cancelCabTextField));
        request.setTermId(textValue(termIdTextField));
        request.setInterNro(intValue(interNroTextField));
        request.setCuit(longValue(cuitTextField));
        request.setErrorExt(intValue(errorExtTextField));

        request.setParam1(
                param1TextField.getText().isBlank()
                        ? "0"
                        : textValue(param1TextField)
        );

        request.setParam2(textValue(param2TextField));

        return request;
    }

    // =========================================================
    // Helpers
    // =========================================================
    private static String toHl7(LocalDate date) {
        return date != null ? date.format(HL7_DATE_FORMAT) : "";
    }

    private String textValue(JTextField field) {
        return field.getText().isBlank() ? null : field.getText().trim();
    }

    private Integer intValue(JTextField field) {
        String txt = field.getText().trim();
        return txt.isBlank() ? null : Integer.parseInt(txt);
    }

    private Long longValue(JTextField field) {
        String txt = field.getText().trim();
        return txt.isBlank() ? null : Long.parseLong(txt);
    }

    private Manual manualValue(JTextField field) {
        if (field.getText().isBlank()) {
            return null;
        }

        return switch (field.getText().trim().toUpperCase()) {
            case "0" -> Manual.MANUAL;
            case "C" -> Manual.CAPITADOR;
            case "L" -> Manual.COMSULTA;
            default -> throw new IllegalArgumentException(
                    "Valor inválido para Manual: " + field.getText()
            );
        };
    }

    // =========================================================
    // Result
    // =========================================================
    private void mostrarResultado(CancelacionResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append("Resultado: ")
                .append(response.getCabecera().getRechaCabeDeno())
                .append("\n");

        if (response.getDetalle() != null && response.getDetalle().length > 0) {
            sb.append("\nDetalle:\n");
            for (var det : response.getDetalle()) {
                sb.append("- ")
                        .append(det.getDenoItem())
                        .append("\n");
            }
        }

        JOptionPane.showMessageDialog(
                this,
                sb.toString(),
                getTitle(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // Close behavior
    // =========================================================
    private void installCloseBehavior() {
        Action cancelAction = DialogUtils.createDisposeAction(this);
        cancelButton.setAction(cancelAction);
        DialogUtils.installCloseAction(this, cancelAction);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        modoLabel = new JLabel();
        modoTextField = new JTextField();
        credenLabel = new JLabel();
        credenTextField = new JTextField();
        tipoLabel = new JLabel();
        tipoTextField = new JTextField();
        altaLabel = new JLabel();
        altaPanel = new JPanel();
        manualLabel = new JLabel();
        manualTextField = new JTextField();
        ticketExtLabel = new JLabel();
        ticketExtTextField = new JTextField();
        cancelCabLabel = new JLabel();
        cancelCabTextField = new JTextField();
        termIdLabel = new JLabel();
        termIdTextField = new JTextField();
        interNroLabel = new JLabel();
        interNroTextField = new JTextField();
        cuitLabel = new JLabel();
        cuitTextField = new JTextField();
        cancelModoLabel = new JLabel();
        cancelModoTextField = new JTextField();
        errorExtLabel = new JLabel();
        errorExtTextField = new JTextField();
        param1Label = new JLabel();
        param1TextField = new JTextField();
        param2Label = new JLabel();
        param2TextField = new JTextField();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

        //---- modoLabel ----
        modoLabel.setText("modo:");
        contentPane.add(modoLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(modoTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- credenLabel ----
        credenLabel.setText("creden:");
        contentPane.add(credenLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(credenTextField, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- tipoLabel ----
        tipoLabel.setText("tipo");
        contentPane.add(tipoLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(tipoTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- altaLabel ----
        altaLabel.setText("alta:");
        contentPane.add(altaLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== altaPanel ========
        {
            altaPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)altaPanel.getLayout()).columnWidths = new int[] {0};
            ((GridBagLayout)altaPanel.getLayout()).rowHeights = new int[] {0};
            ((GridBagLayout)altaPanel.getLayout()).columnWeights = new double[] {1.0E-4};
            ((GridBagLayout)altaPanel.getLayout()).rowWeights = new double[] {1.0E-4};
        }
        contentPane.add(altaPanel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- manualLabel ----
        manualLabel.setText("manual:");
        contentPane.add(manualLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(manualTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- ticketExtLabel ----
        ticketExtLabel.setText("ticketExt:");
        contentPane.add(ticketExtLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(ticketExtTextField, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- cancelCabLabel ----
        cancelCabLabel.setText("cancelCab:");
        contentPane.add(cancelCabLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(cancelCabTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- termIdLabel ----
        termIdLabel.setText("termId:");
        contentPane.add(termIdLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(termIdTextField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- interNroLabel ----
        interNroLabel.setText("interNro:");
        contentPane.add(interNroLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(interNroTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- cuitLabel ----
        cuitLabel.setText("cuit:");
        contentPane.add(cuitLabel, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(cuitTextField, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- cancelModoLabel ----
        cancelModoLabel.setText("cancelModo:");
        contentPane.add(cancelModoLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(cancelModoTextField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- errorExtLabel ----
        errorExtLabel.setText("errorExt:");
        contentPane.add(errorExtLabel, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(errorExtTextField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- param1Label ----
        param1Label.setText("param1:");
        contentPane.add(param1Label, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(param1TextField, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- param2Label ----
        param2Label.setText("param2:");
        contentPane.add(param2Label, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(param2TextField, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel modoLabel;
    private JTextField modoTextField;
    private JLabel credenLabel;
    private JTextField credenTextField;
    private JLabel tipoLabel;
    private JTextField tipoTextField;
    private JLabel altaLabel;
    private JPanel altaPanel;
    private JLabel manualLabel;
    private JTextField manualTextField;
    private JLabel ticketExtLabel;
    private JTextField ticketExtTextField;
    private JLabel cancelCabLabel;
    private JTextField cancelCabTextField;
    private JLabel termIdLabel;
    private JTextField termIdTextField;
    private JLabel interNroLabel;
    private JTextField interNroTextField;
    private JLabel cuitLabel;
    private JTextField cuitTextField;
    private JLabel cancelModoLabel;
    private JTextField cancelModoTextField;
    private JLabel errorExtLabel;
    private JTextField errorExtTextField;
    private JLabel param1Label;
    private JTextField param1TextField;
    private JLabel param2Label;
    private JTextField param2TextField;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}