package com.hl7client.ui.dialogs;

import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.MedicalBenefitItem;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Editor simplificado para prestaciones médicas en modo cancelación.
 * Solo se ingresa el código de prestación (6 dígitos). Cantidad siempre = 1.
 */
public class CancelMedicalBenefitEditorDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.30;
    private static final double SCREEN_RATIO = 0.38;

    private final int remainingChars;
    private final Consumer<BenefitItem> onItemAdded;

    public CancelMedicalBenefitEditorDialog(Window owner, int remainingChars, Consumer<BenefitItem> onItemAdded) {
        super(owner, "Prestación médica a cancelar", ModalityType.APPLICATION_MODAL);
        this.remainingChars = remainingChars;
        this.onItemAdded = onItemAdded;

        initComponents();
        postInit();

        pack();
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);
        setLocationRelativeTo(owner);
    }

    private void postInit() {
        benefitTextField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updatePreview));
        acceptButton.addActionListener(e -> tryAccept());
        acceptButton.setEnabled(false);

        updatePreview();
        SwingUtilities.invokeLater(() -> benefitTextField.requestFocusInWindow());
    }

    private void updatePreview() {
        String code = benefitTextField.getText().trim();
        boolean isValidFormat = code.matches("\\d{6}");

        String message;
        Color color = Color.GRAY;

        if (code.isEmpty()) {
            message = "Ingrese código de prestación (exactamente 6 dígitos)";
        } else if (!isValidFormat) {
            message = "<b>Formato inválido</b> — debe ser exactamente 6 dígitos numéricos";
            color = Color.RED;
        } else {
            String hl7Preview = "1^*" + code + "*1**";
            int length = hl7Preview.length();
            if (length > remainingChars) {
                message = "<b>¡Excede límite!</b> → " + length + " chars (disponibles: " + remainingChars + ")";
                color = Color.RED;
            } else {
                message = "Formato HL7: <code>" + hl7Preview + "</code> (" + length + " chars)";
                color = new Color(0, 128, 0);
            }
        }

        charsPreviewLabel.setText("<html>" + message + "</html>");
        charsPreviewLabel.setForeground(color);
        acceptButton.setEnabled(isValidFormat);
    }

    private void tryAccept() {
        String code = benefitTextField.getText().trim();
        if (code.matches("\\d{6}")) {
            MedicalBenefitItem item = MedicalBenefitItem.of(1, code);
            onItemAdded.accept(item);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "El código debe ser exactamente 6 dígitos numéricos.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Listener simple para el campo de texto
    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;
        SimpleDocumentListener(Runnable action) { this.action = action; }
        @Override public void insertUpdate(DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(DocumentEvent e) { action.run(); }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        benefitLabel = new JLabel();
        benefitTextField = new JTextField();
        charsPreviewLabel = new JLabel();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

        //---- benefitLabel ----
        benefitLabel.setText("Benefit:");
        contentPane.add(benefitLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(benefitTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));
        contentPane.add(charsPreviewLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel benefitLabel;
    private JTextField benefitTextField;
    private JLabel charsPreviewLabel;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}