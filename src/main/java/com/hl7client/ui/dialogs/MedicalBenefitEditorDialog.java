package com.hl7client.ui.dialogs;

import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.MedicalBenefitItem;
import com.hl7client.ui.util.DialogUtils;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Editor para agregar o modificar una prestación médica individual.
 * Controla cantidad (1-99), código (6 dígitos), y longitud restante del campo HL7.
 */
public class MedicalBenefitEditorDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.30;
    private static final double SCREEN_RATIO = 0.42;

    private static final int MAX_QTY_PER_TYPE = 99;
    private static final String CODE_REGEX = "\\d{6}";

    private final int initialRemainingChars;
    private int currentRemainingChars;
    private final Consumer<BenefitItem> onItemAdded;

    private boolean isEditionMode = false;
    private int editingItemLength = 0; // Longitud del ítem que estamos editando (para restar al validar)

    public MedicalBenefitEditorDialog(
            Window owner,
            int remainingChars,
            Consumer<BenefitItem> onItemAdded
    ) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.initialRemainingChars = remainingChars;
        this.currentRemainingChars = remainingChars;
        this.onItemAdded = onItemAdded;

        initComponents();
        postInit();

        pack();
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);
        setLocationRelativeTo(null);
        installCloseBehavior();

        // Foco inicial en el código
        SwingUtilities.invokeLater(() -> benefitTextField.requestFocusInWindow());
    }

    private void postInit() {
        benefitSpinner.setModel(new SpinnerNumberModel(1, 1, MAX_QTY_PER_TYPE, 1));

        UpdateListener updateListener = new UpdateListener();
        benefitSpinner.addChangeListener(updateListener);
        benefitTextField.getDocument().addDocumentListener(updateListener);

        acceptButton.addActionListener(e -> tryAddOrUpdateItem());
        acceptButton.setEnabled(false);

        updateTitle();
        updateCharsPreview();
    }

    /**
     * Precarga valores para modo edición
     */
    public void setInitialValues(int quantity, String code) {
        isEditionMode = true;
        benefitSpinner.setValue(Math.max(1, Math.min(MAX_QTY_PER_TYPE, quantity)));
        benefitTextField.setText(code != null ? code.trim() : "");

        // Calcular longitud del ítem actual para restar al validar espacio
        String preview = quantity + "^*" + code + "*" + quantity + "**";
        editingItemLength = preview.length();

        validateForm();
        updateTitle();
        updateCharsPreview();
    }

    private void tryAddOrUpdateItem() {
        String code = benefitTextField.getText().trim();
        int qty = (Integer) benefitSpinner.getValue();

        if (!code.matches(CODE_REGEX)) {
            JOptionPane.showMessageDialog(this,
                    "<html><b>Código inválido</b><br>Debe ser exactamente 6 dígitos numéricos (ej: 654321)</html>",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MedicalBenefitItem item = MedicalBenefitItem.of(qty, code);
        int itemLength = item.getValue().length();

        // En modo edición: el espacio disponible es mayor porque estamos reemplazando
        int effectiveAvailable = currentRemainingChars + (isEditionMode ? editingItemLength : 0);

        if (itemLength > effectiveAvailable) {
            JOptionPane.showMessageDialog(this,
                    "<html><b>¡Límite excedido!</b><br>Este ítem usaría " + itemLength +
                            " caracteres, pero solo restan " + effectiveAvailable + ".</html>",
                    "Espacio insuficiente", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Notificar al callback
        onItemAdded.accept(item);

        // Actualizar restantes (en edición se ajusta correctamente porque el llamador recalcula)
        currentRemainingChars = effectiveAvailable - itemLength;

        // Limpieza y feedback
        benefitSpinner.setValue(1);
        benefitTextField.setText("");
        benefitTextField.requestFocusInWindow();

        String accion = isEditionMode ? "actualizada" : "agregada";
        JOptionPane.showMessageDialog(this,
                "<html><b>Prestación médica " + accion + " correctamente</b><br><br>" +
                        "Cantidad: <b>" + qty + "</b><br>" +
                        "Código: <b>" + code + "</b><br>" +
                        "Longitud HL7: <b>" + itemLength + "</b> caracteres<br>" +
                        "Restan: <b>" + currentRemainingChars + "</b> caracteres</html>",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);

        // Resetear modo edición (por si se reutiliza el diálogo)
        isEditionMode = false;
        editingItemLength = 0;

        updateTitle();
        updateCharsPreview();
        validateForm();
    }

    private void updateTitle() {
        String modo = isEditionMode ? "Editar" : "Nueva";
        setTitle(modo + " Prestación Médica – Restan " + currentRemainingChars +
                " / " + initialRemainingChars + " caracteres");
    }

    private void updateCharsPreview() {
        int itemLen = calculateItemLength();
        String code = benefitTextField.getText().trim();
        int qty = (Integer) benefitSpinner.getValue();

        StringBuilder msg = new StringBuilder();

        if (code.isEmpty()) {
            msg.append("<b>Ingrese código de prestación</b> (exactamente 6 dígitos)");
        } else if (!code.matches(CODE_REGEX)) {
            msg.append("<b>Código inválido</b> – debe ser 6 dígitos numéricos");
        } else {
            msg.append("Este ítem usaría: <b>").append(itemLen).append("</b> caracteres");
            msg.append("<br>Formato HL7: <code>").append(qty).append("^*").append(code)
                    .append("*").append(qty).append("**</code>");
        }

        charsPreviewLabel.setText("<html>" + msg + "</html>");

        if (itemLen > currentRemainingChars) {
            charsPreviewLabel.setForeground(Color.RED);
        } else if (itemLen > currentRemainingChars * 0.75) {
            charsPreviewLabel.setForeground(new Color(200, 100, 0)); // naranja
        } else if (code.matches(CODE_REGEX)) {
            charsPreviewLabel.setForeground(new Color(0, 140, 0)); // verde
        } else {
            charsPreviewLabel.setForeground(Color.GRAY);
        }
    }

    private void validateForm() {
        String code = benefitTextField.getText().trim();
        benefitSpinner.getValue();

        boolean codeValid;
        codeValid = code.matches(CODE_REGEX);
        int itemLen = calculateItemLength();

        // En edición: considerar el espacio que libera el ítem actual
        int effectiveAvailable = currentRemainingChars + (isEditionMode ? editingItemLength : 0);
        boolean lengthValid = itemLen <= effectiveAvailable;

        acceptButton.setEnabled(codeValid && lengthValid);
        updateCharsPreview();
    }

    private int calculateItemLength() {
        String code = benefitTextField.getText().trim();
        if (code.isEmpty()) return 0;
        int qty = (Integer) benefitSpinner.getValue();
        return (qty + "^*" + code + "*" + qty + "**").length();
    }

    private class UpdateListener implements ChangeListener, DocumentListener {
        @Override public void stateChanged(ChangeEvent e) { validateForm(); }
        @Override public void insertUpdate(DocumentEvent e) { validateForm(); }
        @Override public void removeUpdate(DocumentEvent e) { validateForm(); }
        @Override public void changedUpdate(DocumentEvent e) { validateForm(); }
    }

    private void installCloseBehavior() {
        Action cancelAction = DialogUtils.createDisposeAction(this);
        cancelButton.setAction(cancelAction);
        DialogUtils.installCloseAction(this, cancelAction);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        benefitLabel = new JLabel();
        benefitSpinner = new JSpinner();
        benefitTextField = new JTextField();
        charsPreviewLabel = new JLabel();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

        //---- benefitLabel ----
        benefitLabel.setText("Benefit:");
        contentPane.add(benefitLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(benefitSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(benefitTextField, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));
        contentPane.add(charsPreviewLabel, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel benefitLabel;
    private JSpinner benefitSpinner;
    private JTextField benefitTextField;
    private JLabel charsPreviewLabel;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}