package com.hl7client.ui.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.hl7client.controller.Hl7Controller;
import com.hl7client.model.Hl7Constants;
import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.BenefitRequestMapper;
import com.hl7client.model.dto.request.hl7.Manual;
import com.hl7client.model.dto.request.hl7.RegistracionRequest;
import com.hl7client.model.dto.response.hl7.RegistracionCabecera;
import com.hl7client.model.dto.response.hl7.RegistracionResponse;
import com.hl7client.model.enums.TipoMensaje;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Diálogo principal para registrar una consulta o prestación HL7.
 * Incluye campos de identificación, fechas, prestaciones y envío al controlador.
 */
public class RegistracionDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.75;
    private static final String SPLASH_PATH = "/icons/splash.gif";
    private static final DateTimeFormatter HL7_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final Hl7Controller hl7Controller;

    // Estado interno
    private TipoMensaje tipoMensaje;
    private final List<BenefitItem> benefits = new ArrayList<>();

    // DatePickers
    private DatePicker altaDatePicker;
    private DatePicker fecdifDatePicker;

    public RegistracionDialog(Window owner, Hl7Controller hl7Controller, String titulo) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.hl7Controller = Objects.requireNonNull(hl7Controller);
        setTitle(Objects.requireNonNullElse(titulo, "Registración"));

        initComponents();
        initDatePickers();
        initTipoMensajeControls();
        pack();

        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);
        setLocationRelativeTo(null);

        initActions();
        initShortcuts();
        installCloseBehavior();
        initBenefitsAction();

        // Valores FIJOS obligatorios
        modoTextField.setText("N");
        modoTextField.setEnabled(false);
        tipoTextField.setText("90");
        tipoTextField.setEnabled(false);

        // Foco inicial
        SwingUtilities.invokeLater(() -> credenTextField.requestFocusInWindow());

        updateBenefitsSummary();
    }

    private void initTipoMensajeControls() {
        tipoMensajeComboBox.setModel(new DefaultComboBoxModel<>(TipoMensaje.values()));
        tipoMensajeComboBox.setSelectedItem(null);

        tipoMensajeComboBox.addActionListener(e -> {
            TipoMensaje nuevoTipo = (TipoMensaje) tipoMensajeComboBox.getSelectedItem();
            if (nuevoTipo == null) return;

            if (!benefits.isEmpty() && tipoMensaje != null && tipoMensaje != nuevoTipo) {
                tipoMensajeComboBox.setSelectedItem(tipoMensaje);
                JOptionPane.showMessageDialog(this,
                        "No se puede cambiar el tipo de mensaje una vez que se han cargado prestaciones.\n" +
                                "Limpie las prestaciones primero si desea cambiar el tipo.",
                        "Tipo bloqueado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            tipoMensaje = nuevoTipo;
            updateBenefitsSummary();
        });
    }

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

    private void initBenefitsAction() {
        viewEditBenefitsButton.addActionListener(e -> {
            TipoMensaje effectiveTipo = tipoMensaje;

            if (effectiveTipo == null) {
                effectiveTipo = TipoMensaje.MEDICINA;
                tipoMensajeComboBox.setSelectedItem(effectiveTipo);
                tipoMensaje = effectiveTipo;
                JOptionPane.showMessageDialog(this,
                        "No había tipo de mensaje seleccionado. Se seleccionó 'Medicina' por defecto.\n" +
                                "Puede cambiarlo antes de cargar prestaciones.",
                        "Tipo por defecto", JOptionPane.INFORMATION_MESSAGE);
                updateBenefitsSummary();
            }

            BenefitDialog dialog = getBenefitDialog(effectiveTipo);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                benefits.clear();
                benefits.addAll(dialog.getBenefits());
                updateBenefitsSummary();
            }
        });
    }

    private BenefitDialog getBenefitDialog(TipoMensaje effectiveTipo) {
        BenefitDialog dialog = new BenefitDialog(this, effectiveTipo, new ArrayList<>(benefits));

        int maxChars = (effectiveTipo == TipoMensaje.ODONTOLOGIA)
                ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA
                : Hl7Constants.MAX_LENGTH_MEDICINA;

        String tipoDesc = (effectiveTipo == TipoMensaje.ODONTOLOGIA) ? "Odontología" : "Medicina";
        dialog.setTitle("Gestión de Prestaciones - " + tipoDesc + " (máx. " + maxChars + " caracteres)");

        return dialog;
    }

    private void updateBenefitsSummary() {
        if (benefits.isEmpty()) {
            benefitsSummaryLabel.setText("<html><i>Ninguna prestación cargada</i></html>");
            benefitsSummaryLabel.setForeground(Color.GRAY);
            benefitsSummaryLabel.setToolTipText("Haga clic en 'View / Edit Benefits' para agregar o editar prestaciones");
            acceptButton.setEnabled(true);
            acceptButton.setToolTipText(null);
            return;
        }

        boolean isDental = benefits.get(0) instanceof com.hl7client.model.dental.DentalBenefit;
        int maxChars = isDental ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA : Hl7Constants.MAX_LENGTH_MEDICINA;
        int usedChars = benefits.stream().mapToInt(BenefitItem::length).sum();
        int remaining = maxChars - usedChars;
        int count = benefits.size();

        double porcentaje = usedChars * 100.0 / maxChars;

        String tipoText = isDental ? "odontológica" : "médica";
        String restriccion = isDental ? " (máximo 1 permitida)" : "";

        String text = String.format(
                "<html>%d prestación%s %s<br>usadas <b>%d / %d</b> caracteres (%.1f%%)<br>restan <b>%d</b>%s</html>",
                count, (count == 1 ? "" : "es"), tipoText, usedChars, maxChars, porcentaje, remaining, restriccion
        );

        benefitsSummaryLabel.setText(text);

        // Colores y tooltips consistentes con BenefitDialog
        if (usedChars > maxChars) {
            benefitsSummaryLabel.setForeground(Color.RED);
            benefitsSummaryLabel.setToolTipText("<html><b>¡Excede el límite máximo permitido!</b><br>" +
                    "Corrija antes de aceptar la registración.</html>");
            acceptButton.setToolTipText("No se puede aceptar: excede el límite de caracteres en prestaciones");
        } else if (porcentaje > 90 || remaining < 50) {
            benefitsSummaryLabel.setForeground(new Color(200, 80, 0)); // naranja oscuro
            benefitsSummaryLabel.setToolTipText("<html><b>Muy cerca del límite máximo</b><br>" +
                    "Restan solo " + remaining + " caracteres<br>Revise antes de continuar.</html>");
            acceptButton.setToolTipText(null);
        } else if (porcentaje > 70 || remaining < 150) {
            benefitsSummaryLabel.setForeground(new Color(180, 140, 0)); // ámbar
            benefitsSummaryLabel.setToolTipText("<html>Acercándose al límite<br>Restan " + remaining + " caracteres</html>");
            acceptButton.setToolTipText(null);
        } else if (isDental) {
            benefitsSummaryLabel.setForeground(new Color(0, 140, 0)); // verde oscuro
            benefitsSummaryLabel.setToolTipText("<html>Prestación odontológica válida<br>(solo 1 permitida)</html>");
            acceptButton.setToolTipText(null);
        } else {
            benefitsSummaryLabel.setForeground(new Color(0, 100, 200)); // azul
            benefitsSummaryLabel.setToolTipText("<html>Prestaciones médicas cargadas correctamente<br>" +
                    "Restan " + remaining + " caracteres</html>");
            acceptButton.setToolTipText(null);
        }

        updateAcceptButtonState();
    }

    private void updateAcceptButtonState() {
        if (benefits.isEmpty()) {
            acceptButton.setEnabled(true);
            acceptButton.setToolTipText(null);
            return;
        }

        boolean isDental = benefits.get(0) instanceof com.hl7client.model.dental.DentalBenefit;
        int maxChars = isDental ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA : Hl7Constants.MAX_LENGTH_MEDICINA;
        int usedChars = benefits.stream().mapToInt(BenefitItem::length).sum();

        acceptButton.setEnabled(usedChars <= maxChars);

        if (usedChars > maxChars) {
            acceptButton.setToolTipText("No se puede aceptar: excede el límite de caracteres en prestaciones");
        } else {
            acceptButton.setToolTipText(null);
        }
    }

    private void initActions() {
        Action acceptAction = new AcceptAction<>(
                "Accept",
                this,
                getClass().getResource(SPLASH_PATH),
                this::doRegistracion,
                this::onRegistracionResult
        );
        acceptButton.setAction(acceptAction);
        getRootPane().setDefaultButton(acceptButton);
    }

    private void initShortcuts() {
        acceptButton.setMnemonic(KeyEvent.VK_A);   // ALT + A
        cancelButton.setMnemonic(KeyEvent.VK_C);   // ALT + C
    }

    private Hl7Result<RegistracionResponse> doRegistracion() {
        if (tipoMensaje == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un tipo de mensaje (Odontología o Medicina).",
                    "Tipo de mensaje requerido", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return hl7Controller.consultarRegistracion(buildRequest());
    }

    private void onRegistracionResult(Hl7Result<RegistracionResponse> result) {
        if (result == null) {
            JOptionPane.showMessageDialog(this,
                    "Error técnico inesperado", getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String transac = result.getData()
                .map(RegistracionResponse::getCabecera)
                .map(RegistracionCabecera::getTransac)
                .map(String::valueOf)
                .orElse(null);

        Hl7UiErrorHandler.mostrarResultado(this, result, getTitle(), transac);

        if (result.isOk() || result.isPartial()) {
            result.getData().ifPresent(this::mostrarResultado);
            dispose();
        }
    }

    private RegistracionRequest buildRequest() {
        RegistracionRequest request = new RegistracionRequest();

        request.setModo(textValue(modoTextField));
        request.setTipo(intValue(tipoTextField));
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
                        ? Integer.valueOf(0)
                        : intValue(autorizTextField)
        );
        request.setIcd(textValue(icdTextField));
        request.setRechaExt(intValue(rechaExtTextField));
        request.setTipoEfector(textValue(tipoEfectorTextField));
        request.setIdEfector(textValue(idEfectorTextField));
        request.setTipoPrescr(textValue(tipoPrescrTextField));
        request.setIdPrescr(textValue(idPrescrTextField));
        request.setMsgId(textValue(msgIdTextField));
        request.setAckacept(textValue(ackaceptTextField));
        request.setAckackapl(textValue(ackackaplTextField));
        request.setConsulta(manualValue(consultaTextField));

        TipoMensaje actualTipoMensaje = tipoMensaje != null ? tipoMensaje : TipoMensaje.MEDICINA;
        request.setTipoMensaje(actualTipoMensaje.getCodigoHl7());

        BenefitRequestMapper.apply(request, benefits);

        if (!powerBuilderTextField.getText().isBlank()) {
            request.setPowerBuilder(powerBuilderTextField.getText().trim().equals("1"));
        }

        return request;
    }

    private void mostrarResultado(RegistracionResponse response) {
        var cab = response.getCabecera();
        String mensaje =
                "Afiliado: " + cab.getApeNom().trim() + "\n" +
                        "Plan: " + cab.getPlanCodi().trim() + "\n" +
                        "Edad: " + cab.getEdad() + "\n" +
                        "Sexo: " + cab.getSexo() + "\n" +
                        "PMI: " + (cab.getPmi() != null ? cab.getPmi() : "-");
        JOptionPane.showMessageDialog(this, mensaje, getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    // Helpers =================================================================

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
        icdLabel = new JLabel();
        icdTextField = new JTextField();
        rechaExtLabel = new JLabel();
        rechaExtTextField = new JTextField();
        viewEditBenefitsLabel = new JLabel();
        viewEditBenefitsButton = new JButton();
        benefitsSummaryLabel = new JLabel();
        tipoEfectorLabel = new JLabel();
        tipoEfectorTextField = new JTextField();
        idEfectorLabel = new JLabel();
        idEfectorTextField = new JTextField();
        tipoPrescrLabel = new JLabel();
        tipoPrescrTextField = new JTextField();
        idPrescrLabel = new JLabel();
        idPrescrTextField = new JTextField();
        msgIdLabel = new JLabel();
        msgIdTextField = new JTextField();
        ackaceptLabel = new JLabel();
        ackaceptTextField = new JTextField();
        ackackaplLabel = new JLabel();
        ackackaplTextField = new JTextField();
        consultaLabel = new JLabel();
        consultaTextField = new JTextField();
        agRechaCabeLabel = new JLabel();
        agRechaCabeTextField = new JTextField();
        agRechaLabel = new JLabel();
        agRechaTextField = new JTextField();
        tipoMensajeLabel = new JLabel();
        tipoMensajeComboBox = new JComboBox();
        powerBuilderLabel = new JLabel();
        powerBuilderTextField = new JTextField();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

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
        tipoLabel.setText("tipo:");
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

        //---- fecdifLabel ----
        fecdifLabel.setText("fecdif:");
        contentPane.add(fecdifLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
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
        contentPane.add(fecdifPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- manualLabel ----
        manualLabel.setText("manual:");
        contentPane.add(manualLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(manualTextField, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- ticketExtLabel ----
        ticketExtLabel.setText("ticketExt:");
        contentPane.add(ticketExtLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(ticketExtTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
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

        //---- oriMatriLabel ----
        oriMatriLabel.setText("oriMatri:");
        contentPane.add(oriMatriLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(oriMatriTextField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- autorizLabel ----
        autorizLabel.setText("autoriz:");
        contentPane.add(autorizLabel, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(autorizTextField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- icdLabel ----
        icdLabel.setText("icd:");
        contentPane.add(icdLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(icdTextField, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- rechaExtLabel ----
        rechaExtLabel.setText("rechaExt:");
        contentPane.add(rechaExtLabel, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(rechaExtTextField, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- viewEditBenefitsLabel ----
        viewEditBenefitsLabel.setText("View / Edit Benefits:");
        contentPane.add(viewEditBenefitsLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- viewEditBenefitsButton ----
        viewEditBenefitsButton.setText("View / Edit Benefits");
        contentPane.add(viewEditBenefitsButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(benefitsSummaryLabel, new GridBagConstraints(2, 7, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- tipoEfectorLabel ----
        tipoEfectorLabel.setText("tipoEfector:");
        contentPane.add(tipoEfectorLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(tipoEfectorTextField, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- idEfectorLabel ----
        idEfectorLabel.setText("idEfector:");
        contentPane.add(idEfectorLabel, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(idEfectorTextField, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- tipoPrescrLabel ----
        tipoPrescrLabel.setText("tipoPrescr:");
        contentPane.add(tipoPrescrLabel, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(tipoPrescrTextField, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- idPrescrLabel ----
        idPrescrLabel.setText("idPrescr:");
        contentPane.add(idPrescrLabel, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(idPrescrTextField, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- msgIdLabel ----
        msgIdLabel.setText("msgId:");
        contentPane.add(msgIdLabel, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(msgIdTextField, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- ackaceptLabel ----
        ackaceptLabel.setText("ackacept:");
        contentPane.add(ackaceptLabel, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(ackaceptTextField, new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- ackackaplLabel ----
        ackackaplLabel.setText("ackackapl:");
        contentPane.add(ackackaplLabel, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(ackackaplTextField, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- consultaLabel ----
        consultaLabel.setText("consulta:");
        contentPane.add(consultaLabel, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(consultaTextField, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- agRechaCabeLabel ----
        agRechaCabeLabel.setText("agRechaCabe:");
        contentPane.add(agRechaCabeLabel, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(agRechaCabeTextField, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- agRechaLabel ----
        agRechaLabel.setText("agRecha:");
        contentPane.add(agRechaLabel, new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(agRechaTextField, new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- tipoMensajeLabel ----
        tipoMensajeLabel.setText("tipoMensaje:");
        contentPane.add(tipoMensajeLabel, new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(tipoMensajeComboBox, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- powerBuilderLabel ----
        powerBuilderLabel.setText("powerBuilder:");
        contentPane.add(powerBuilderLabel, new GridBagConstraints(2, 13, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(powerBuilderTextField, new GridBagConstraints(3, 13, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(3, 14, 1, 1, 0.0, 0.0,
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
    private JLabel icdLabel;
    private JTextField icdTextField;
    private JLabel rechaExtLabel;
    private JTextField rechaExtTextField;
    private JLabel viewEditBenefitsLabel;
    private JButton viewEditBenefitsButton;
    private JLabel benefitsSummaryLabel;
    private JLabel tipoEfectorLabel;
    private JTextField tipoEfectorTextField;
    private JLabel idEfectorLabel;
    private JTextField idEfectorTextField;
    private JLabel tipoPrescrLabel;
    private JTextField tipoPrescrTextField;
    private JLabel idPrescrLabel;
    private JTextField idPrescrTextField;
    private JLabel msgIdLabel;
    private JTextField msgIdTextField;
    private JLabel ackaceptLabel;
    private JTextField ackaceptTextField;
    private JLabel ackackaplLabel;
    private JTextField ackackaplTextField;
    private JLabel consultaLabel;
    private JTextField consultaTextField;
    private JLabel agRechaCabeLabel;
    private JTextField agRechaCabeTextField;
    private JLabel agRechaLabel;
    private JTextField agRechaTextField;
    private JLabel tipoMensajeLabel;
    private JComboBox tipoMensajeComboBox;
    private JLabel powerBuilderLabel;
    private JTextField powerBuilderTextField;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}