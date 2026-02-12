package com.hl7client.ui.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.hl7client.controller.Hl7Controller;
import com.hl7client.model.dto.request.hl7.ElegibilidadRequest;
import com.hl7client.model.dto.request.hl7.Manual;
import com.hl7client.model.dto.response.hl7.ElegibilidadResponse;
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

public class ElegibilidadDialog extends JDialog {

    private final Hl7Controller hl7Controller;

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.75;
    private static final String SPLASH_PATH = "/icons/splash.gif";

    private static final DateTimeFormatter HL7_DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;

    // =========================
    // DatePickers
    // =========================
    private DatePicker altaDatePicker;
    private DatePicker fecdifDatePicker;

    public ElegibilidadDialog(
            Window owner,
            Hl7Controller hl7Controller,
            String titulo
    ) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.hl7Controller = Objects.requireNonNull(hl7Controller);
        setTitle(Objects.requireNonNullElse(titulo, "Elegibilidad"));

        initComponents();
        initDatePickers();
        initActions();
        initShortcuts();
        installCloseBehavior();

        pack();

        // Establecemos tamaño mínimo proporcional a la pantalla
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);  // ≈ 22% → ajustable

        // Aplicamos tamaño inicial deseado
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        setLocationRelativeTo(null);

        // Valores FIJOS obligatorios - se asignan después de initComponents()
        modoTextField.setText("N");
        modoTextField.setEnabled(false);

        // Foco inicial (modo está deshabilitado, pasamos al siguiente campo editable)
        SwingUtilities.invokeLater(() ->
                credenTextField.requestFocusInWindow()
        );
    }

    // =========================================================
    // DatePickers
    // =========================================================
    private void initDatePickers() {
        DatePickerSettings altaSettings = new DatePickerSettings();
        altaSettings.setFormatForDatesCommonEra("yyyyMMdd");
        altaSettings.setAllowKeyboardEditing(false);

        altaDatePicker = new DatePicker(altaSettings);

        altaPanel.setLayout(new BorderLayout());
        altaPanel.add(altaDatePicker, BorderLayout.CENTER);

        DatePickerSettings fecdifSettings = new DatePickerSettings();
        fecdifSettings.setFormatForDatesCommonEra("yyyyMMdd");
        fecdifSettings.setAllowKeyboardEditing(false);

        fecdifDatePicker = new DatePicker(fecdifSettings);

        fecdifPanel.setLayout(new BorderLayout());
        fecdifPanel.add(fecdifDatePicker, BorderLayout.CENTER);
    }

    // =========================================================
    // Actions
    // =========================================================
    private void initActions() {
        Action acceptAction = new AcceptAction<>(
                "Accept",
                this,
                getClass().getResource(SPLASH_PATH),
                this::doElegibilidad,
                this::onElegibilidadResult
        );

        acceptButton.setAction(acceptAction);

        // ENTER → Accept
        getRootPane().setDefaultButton(acceptButton);
    }

    private void initShortcuts() {
        // ALT + tecla
        acceptButton.setMnemonic(KeyEvent.VK_A);   // ALT + A
        cancelButton.setMnemonic(KeyEvent.VK_C);   // ALT + C
    }

    // =========================================================
    // Accept workflow
    // =========================================================
    private Hl7Result<ElegibilidadResponse> doElegibilidad() {
        return hl7Controller.consultarElegibilidad(buildRequest());
    }

    private void onElegibilidadResult(Hl7Result<ElegibilidadResponse> result) {
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
                .map(ElegibilidadResponse::getTransac)
                .orElse(null);

        Hl7UiErrorHandler.mostrarResultado(
                this,
                result,
                getTitle(),
                transac
        );

        if (result.isOk() || result.isPartial()) {
            result.getData().ifPresent(this::mostrarResultado);
            dispose();
        }
    }

    // =========================================================
    // Build Request
    // =========================================================
    private ElegibilidadRequest buildRequest() {
        ElegibilidadRequest request = new ElegibilidadRequest();

        // Valores FIJOS (leídos de campos deshabilitados)
        request.setModo(textValue(modoTextField));  // "N"
        request.setCreden(textValue(credenTextField));
        request.setAlta(toHl7(altaDatePicker.getDate()));
        request.setFecdif(toHl7(fecdifDatePicker.getDate()));
        request.setManual(manualValue(manualTextField));
        request.setTicketExt(intValue(ticketExtTextField));
        request.setTermId(textValue(termIdTextField));
        request.setInterNro(intValue(interNroTextField));
        request.setCuit(textValue(cuitTextField));
        request.setOriMatri(textValue(oriMatriTextField));
        request.setAutoriz(
                autorizTextField.getText().isBlank()
                        ? 0
                        : intValue(autorizTextField)
        );
        request.setRechaExt(intValue(rechaExtTextField));

        return request;
    }

    // =========================================================
    // HL7 helper
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

    private Manual manualValue(JTextField field) {
        String txt = field.getText().trim().toUpperCase();
        if (txt.isBlank()) return null;
        return switch (txt) {
            case "0" -> Manual.MANUAL;
            case "C" -> Manual.CAPITADOR;
            case "L" -> Manual.COMSULTA;
            default -> throw new IllegalArgumentException("Valor inválido para Manual: " + txt);
        };
    }

    // =========================================================
    // Result
    // =========================================================
    private void mostrarResultado(ElegibilidadResponse response) {
        String mensaje =
                "Afiliado: " + response.getApeNom() + "\n" +
                        "Plan: " + response.getPlanCodi() + "\n" +
                        "Edad: " + response.getEdad() + "\n" +
                        "PMI: " + response.getPmi();

        JOptionPane.showMessageDialog(
                this,
                mensaje,
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
        altaLabel = new JLabel();
        altaPanel = new JPanel();
        fecdifLabel = new JLabel();
        fecdifPanel = new JPanel();
        manualLabel = new JLabel();
        manualTextField = new JTextField();
        ticketExtLabel = new JLabel();
        ticketExtTextField = new JTextField();
        termIdLabel = new JLabel();
        termIdTextField = new JTextField();
        interNroLabel = new JLabel();
        interNroTextField = new JTextField();
        cuitLabel = new JLabel();
        cuitTextField = new JTextField();
        oriMatriLabel = new JLabel();
        oriMatriTextField = new JTextField();
        autorizLabel = new JLabel();
        autorizTextField = new JTextField();
        rechaExtLabel = new JLabel();
        rechaExtTextField = new JTextField();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

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

        //---- altaLabel ----
        altaLabel.setText("alta:");
        contentPane.add(altaLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
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
        contentPane.add(altaPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- fecdifLabel ----
        fecdifLabel.setText("fecdif:");
        contentPane.add(fecdifLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== fecdifPanel ========
        {
            fecdifPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)fecdifPanel.getLayout()).columnWidths = new int[] {0};
            ((GridBagLayout)fecdifPanel.getLayout()).rowHeights = new int[] {0};
            ((GridBagLayout)fecdifPanel.getLayout()).columnWeights = new double[] {1.0E-4};
            ((GridBagLayout)fecdifPanel.getLayout()).rowWeights = new double[] {1.0E-4};
        }
        contentPane.add(fecdifPanel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
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

        //---- termIdLabel ----
        termIdLabel.setText("termId:");
        contentPane.add(termIdLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(termIdTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- interNroLabel ----
        interNroLabel.setText("interNro:");
        contentPane.add(interNroLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(interNroTextField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- cuitLabel ----
        cuitLabel.setText("cuit:");
        contentPane.add(cuitLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(cuitTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- oriMatriLabel ----
        oriMatriLabel.setText("oriMatri:");
        contentPane.add(oriMatriLabel, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(oriMatriTextField, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- autorizLabel ----
        autorizLabel.setText("autoriz:");
        contentPane.add(autorizLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(autorizTextField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- rechaExtLabel ----
        rechaExtLabel.setText("rechaExt:");
        contentPane.add(rechaExtLabel, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(rechaExtTextField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
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
    private JLabel altaLabel;
    private JPanel altaPanel;
    private JLabel fecdifLabel;
    private JPanel fecdifPanel;
    private JLabel manualLabel;
    private JTextField manualTextField;
    private JLabel ticketExtLabel;
    private JTextField ticketExtTextField;
    private JLabel termIdLabel;
    private JTextField termIdTextField;
    private JLabel interNroLabel;
    private JTextField interNroTextField;
    private JLabel cuitLabel;
    private JTextField cuitTextField;
    private JLabel oriMatriLabel;
    private JTextField oriMatriTextField;
    private JLabel autorizLabel;
    private JTextField autorizTextField;
    private JLabel rechaExtLabel;
    private JTextField rechaExtTextField;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}