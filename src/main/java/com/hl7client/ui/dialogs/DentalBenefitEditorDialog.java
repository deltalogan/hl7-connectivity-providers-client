package com.hl7client.ui.dialogs;

import javax.swing.border.*;
import com.hl7client.model.Hl7Constants;
import com.hl7client.model.dental.*;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Editor de prestaciones odontológicas.
 * Permite crear o editar una prestación con:
 * - Selección opcional de pieza dental (FDI)
 * - Selección opcional de superficies (0 o más)
 * - Código de prestación obligatorio (numérico, prefijo "O" automático)
 * - Construcción y validación del string HL7 (permite sin pieza ni superficies)
 */
public class DentalBenefitEditorDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.30;
    private static final double SCREEN_RATIO = 0.40;

    private static final String TOTAL_COUNT = "1";
    private static final String ORIGIN = "P";
    private static final String ITEM_QUANTITY = "1";
    private static final String ASTERISKS = "**";

    private final Consumer<DentalBenefit> onAcceptCallback;
    private DentalBenefit initialBenefit; // Para saber si es edición o inserción
    private DentalBenefit result;

    public DentalBenefitEditorDialog(Window owner, Consumer<DentalBenefit> onAccept) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.onAcceptCallback = onAccept;
        initComponents();
        postInit();

        pack();

        // Establecemos tamaño mínimo proporcional a la pantalla
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);  // ≈ 22% → ajustable

        // Aplicamos tamaño inicial deseado
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        setLocationRelativeTo(null);
    }

    /**
     * Precarga valores de una prestación existente (modo edición)
     */
    public void setInitialBenefit(DentalBenefit benefit) {
        this.initialBenefit = benefit;
        if (benefit == null) return;

        // Código sin prefijo "O"
        String code = benefit.getBenefitCode();
        if (code != null && code.startsWith("O") && code.length() > 1) {
            benefitTextField.setText(code.substring(1));
        } else if (code != null) {
            benefitTextField.setText(code);
        }

        // Pieza (opcional)
        if (benefit.getPiece() != null) {
            int fdi = Integer.parseInt(benefit.getPiece().getFdiCode());
            for (int row = 0; row < toothSelectionTable.getRowCount(); row++) {
                Integer rowFdi = (Integer) toothSelectionTable.getValueAt(row, 0);
                if (rowFdi != null && rowFdi == fdi) {
                    toothSelectionTable.setValueAt(true, row, 2);
                    Rectangle rect = toothSelectionTable.getCellRect(row, 0, true);
                    toothSelectionTable.scrollRectToVisible(rect);
                    break;
                }
            }
        }

        // Superficies (opcional)
        Set<DentalSurface> surfaces = benefit.getSurfaces();
        mesialCheckBox.setSelected(surfaces.contains(DentalSurface.MESIAL));
        distalCheckBox.setSelected(surfaces.contains(DentalSurface.DISTAL));
        vestibularCheckBox.setSelected(surfaces.contains(DentalSurface.VESTIBULAR));
        lingualCheckBox.setSelected(surfaces.contains(DentalSurface.LINGUAL));
        occlusalCheckBox.setSelected(surfaces.contains(DentalSurface.OCCLUSAL));
        incisalCheckBox.setSelected(surfaces.contains(DentalSurface.INCISAL));
        palatalCheckBox.setSelected(surfaces.contains(DentalSurface.PALATAL));

        updatePreview();
    }

    private void postInit() {
        updateTitle(); // Inicial

        toothSelectionTable.setModel(createToothTableModel());
        toothSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer bonito para checkbox
        toothSelectionTable.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            private final JCheckBox checkBox = new JCheckBox();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                checkBox.setSelected(value != null && (Boolean) value);
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return checkBox;
            }
        });

        toothSelectionTable.setDefaultEditor(Object.class, null);

        // Selección única de pieza (opcional)
        toothSelectionTable.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                int editedRow = e.getFirstRow();
                Boolean checked = (Boolean) toothSelectionTable.getValueAt(editedRow, 2);
                if (Boolean.TRUE.equals(checked)) {
                    for (int row = 0; row < toothSelectionTable.getRowCount(); row++) {
                        if (row != editedRow) {
                            toothSelectionTable.setValueAt(false, row, 2);
                        }
                    }
                }
                updatePreview();
            }
        });

        ActionListener updateListener = e -> updatePreview();
        mesialCheckBox.addActionListener(updateListener);
        distalCheckBox.addActionListener(updateListener);
        vestibularCheckBox.addActionListener(updateListener);
        lingualCheckBox.addActionListener(updateListener);
        occlusalCheckBox.addActionListener(updateListener);
        incisalCheckBox.addActionListener(updateListener);
        palatalCheckBox.addActionListener(updateListener);

        benefitTextField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updatePreview));

        acceptButton.addActionListener(e -> onAccept());
        cancelButton.addActionListener(e -> dispose());

        updatePreview();
    }

    private void updateTitle() {
        String modo = (initialBenefit != null) ? "Editar" : "Nueva";
        String piezaInfo = getSelectedPiece() != null ? " - Pieza " + getSelectedPiece().getFdiCode() : "";
        setTitle(modo + " Prestación Odontológica" + piezaInfo);
    }

    private void updatePreview() {
        updateTitle();

        DentalPiece selectedPiece = getSelectedPiece();
        Set<DentalSurface> selectedSurfaces = getSelectedSurfaces();
        String benefitCode = getBenefitCode();
        String hl7 = buildHl7String();
        int len = hl7.length();

        StringBuilder msg = new StringBuilder();
        Color color; // verde por defecto (válido)

        if (benefitCode.isEmpty()) {
            msg.append("<b>Ingrese el código de prestación</b><br>(solo números, ej: 020801)");
            color = new Color(200, 100, 0); // ámbar
        } else if (selectedPiece == null) {
            if (selectedSurfaces.isEmpty()) {
                msg.append("<b>Prestación general</b><br>Sin pieza ni caras específicas – <b>Válida</b>");
                color = new Color(0, 140, 0);
            } else {
                msg.append("<b>¡Atención!</b><br>Superficies seleccionadas sin pieza<br>Las caras serán ignoradas en HL7");
                color = new Color(200, 100, 0);
            }
        } else {
            // Con pieza – ahora permitimos 0 superficies
            DentalValidationResult validation = DentalSurfaceMatrix.validateCombination(selectedPiece, selectedSurfaces);
            if (!validation.isValid()) {
                msg.append("<b>Error de combinación:</b> ").append(validation.getMessage());
                color = Color.RED;
            } else if (len > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
                msg.append("<b>Longitud excede el límite:</b> ").append(len)
                        .append(" / ").append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA);
                color = Color.RED;
            } else {
                msg.append("Longitud actual: <b>").append(len).append(" / ")
                        .append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA).append("</b> caracteres");
                if (selectedSurfaces.isEmpty()) {
                    msg.append("<br><i>Sin caras seleccionadas – Válido</i>");
                } else {
                    msg.append("<br>Caras: ").append(selectedSurfaces.stream()
                            .map(DentalSurface::name)
                            .collect(Collectors.joining(", ")));
                }
                color = (len > Hl7Constants.MAX_LENGTH_ODONTOLOGIA * 0.85)
                        ? new Color(200, 100, 0)
                        : new Color(0, 140, 0);
            }
        }

        lengthPreviewLabel.setText("<html>" + msg + "</html>");
        lengthPreviewLabel.setForeground(color);

        acceptButton.setEnabled(isValidInput());
    }

    private boolean isValidInput() {
        String code = getBenefitCode();
        if (code.isEmpty() || !code.matches("O\\d{5,6}")) return false; // al menos 5 dígitos + O

        DentalPiece piece = getSelectedPiece();
        Set<DentalSurface> surfaces = getSelectedSurfaces();

        // Validación odontológica solo si hay pieza
        if (piece != null) {
            DentalValidationResult validation = DentalSurfaceMatrix.validateCombination(piece, surfaces);
            if (!validation.isValid()) return false;
        } else {
            // Sin pieza: solo permitimos superficies vacías
            if (!surfaces.isEmpty()) return false;
        }

        String hl7 = buildHl7String();
        return hl7.length() <= Hl7Constants.MAX_LENGTH_ODONTOLOGIA && !hl7.isEmpty();
    }

    private void onAccept() {
        if (!isValidInput()) {
            StringBuilder msg = new StringBuilder("<html><b>Validación fallida:</b><br><br>");

            String code = getBenefitCode();
            if (code.isEmpty() || !code.matches("O\\d{5,6}")) {
                msg.append("• Código inválido (debe ser O + números, ej: O020801)<br>");
            }

            DentalPiece piece = getSelectedPiece();
            Set<DentalSurface> surfaces = getSelectedSurfaces();

            if (piece != null) {
                DentalValidationResult validation = DentalSurfaceMatrix.validateCombination(piece, surfaces);
                if (!validation.isValid()) {
                    msg.append("• ").append(validation.getMessage()).append("<br>");
                }
            } else if (!surfaces.isEmpty()) {
                msg.append("• No se pueden seleccionar caras sin pieza dental<br>");
            }

            String hl7 = buildHl7String();
            if (hl7.length() > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
                msg.append("• Longitud excede ").append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA)
                        .append(" caracteres (actual: ").append(hl7.length()).append(")<br>");
            }

            JOptionPane.showMessageDialog(this, msg.append("</html>").toString(),
                    "Validación odontológica", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int fdi = getSelectedFdi();
        DentalPiece piece = (fdi > 0) ? DentalPiece.fromFdi(fdi) : null;
        Set<DentalSurface> surfaces = getSelectedSurfaces();
        String numericCode = benefitTextField.getText().trim();

        this.result = new DentalBenefit(piece, surfaces, numericCode);

        if (onAcceptCallback != null) {
            onAcceptCallback.accept(result);
        }

        String piezaDesc = (piece != null) ? piece.getFdiCode() : "Ninguna (prestación general)";
        String superficiesDesc = surfaces.isEmpty()
                ? "Ninguna (válido)"
                : surfaces.stream().map(Enum::name).sorted().collect(Collectors.joining(", "));

        JOptionPane.showMessageDialog(this,
                "<html><b>Prestación odontológica guardada correctamente</b><br><br>" +
                        "Pieza: <b>" + piezaDesc + "</b><br>" +
                        "Superficies: <b>" + superficiesDesc + "</b><br>" +
                        "Código: <b>O" + numericCode + "</b><br>" +
                        "Longitud HL7: <b>" + result.length() + "</b> caracteres</html>",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    // Helpers
    private int getSelectedFdi() {
        for (int row = 0; row < toothSelectionTable.getRowCount(); row++) {
            Boolean included = (Boolean) toothSelectionTable.getValueAt(row, 2);
            if (Boolean.TRUE.equals(included)) {
                return (Integer) toothSelectionTable.getValueAt(row, 0);
            }
        }
        return -1;
    }

    private DentalPiece getSelectedPiece() {
        int fdi = getSelectedFdi();
        return fdi > 0 ? DentalPiece.fromFdi(fdi) : null;
    }

    private Set<DentalSurface> getSelectedSurfaces() {
        Set<DentalSurface> surfaces = EnumSet.noneOf(DentalSurface.class);
        if (mesialCheckBox.isSelected()) surfaces.add(DentalSurface.MESIAL);
        if (distalCheckBox.isSelected()) surfaces.add(DentalSurface.DISTAL);
        if (vestibularCheckBox.isSelected()) surfaces.add(DentalSurface.VESTIBULAR);
        if (lingualCheckBox.isSelected()) surfaces.add(DentalSurface.LINGUAL);
        if (occlusalCheckBox.isSelected()) surfaces.add(DentalSurface.OCCLUSAL);
        if (incisalCheckBox.isSelected()) surfaces.add(DentalSurface.INCISAL);
        if (palatalCheckBox.isSelected()) surfaces.add(DentalSurface.PALATAL);
        return surfaces;
    }

    private String getSurfacesCode() {
        Set<DentalSurface> selected = getSelectedSurfaces();
        if (selected.isEmpty()) return "";

        List<DentalSurface> ordered = new ArrayList<>(selected);
        ordered.sort(Comparator.comparingInt(s -> switch (s) {
            case VESTIBULAR -> 0;
            case OCCLUSAL   -> 1;
            case DISTAL     -> 2;
            case MESIAL     -> 3;
            case LINGUAL    -> 4;
            case INCISAL    -> 5;
            case PALATAL    -> 6;
        }));

        return ordered.stream()
                .map(DentalSurface::getCode)
                .collect(Collectors.joining());
    }

    private String getBenefitCode() {
        String input = benefitTextField.getText().trim();
        if (input.isEmpty() || !input.matches("\\d+")) return "";
        return "O" + input;
    }

    private String buildHl7String() {
        int fdi = getSelectedFdi();
        String pieceStr = (fdi > 0) ? String.valueOf(fdi) : "";

        String surfacesStr = getSurfacesCode();
        String benefitCodeStr = getBenefitCode();

        if (benefitCodeStr.isEmpty()) return "";

        return String.format("%s^*%s*%s*%s*%s*%s%s",
                TOTAL_COUNT, pieceStr, surfacesStr, benefitCodeStr, ORIGIN, ITEM_QUANTITY, ASTERISKS);
    }

    private DefaultTableModel createToothTableModel() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"FDI", "Descripción", "Incluida"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : String.class;
            }
        };

        Object[][] teeth = {
                {11, "Incisivo central superior derecho", false},
                {12, "Incisivo lateral superior derecho", false},
                {13, "Canino superior derecho", false},
                {14, "1er premolar superior derecho", false},
                {15, "2do premolar superior derecho", false},
                {16, "1er molar superior derecho", false},
                {17, "2do molar superior derecho", false},
                {18, "Tercer molar superior derecho", false},
                {21, "Incisivo central superior izquierdo", false},
                {22, "Incisivo lateral superior izquierdo", false},
                {23, "Canino superior izquierdo", false},
                {24, "1er premolar superior izquierdo", false},
                {25, "2do premolar superior izquierdo", false},
                {26, "1er molar superior izquierdo", false},
                {27, "2do molar superior izquierdo", false},
                {28, "Tercer molar superior izquierdo", false},
                {31, "Incisivo central inferior izquierdo", false},
                {32, "Incisivo lateral inferior izquierdo", false},
                {33, "Canino inferior izquierdo", false},
                {34, "1er premolar inferior izquierdo", false},
                {35, "2do premolar inferior izquierdo", false},
                {36, "1er molar inferior izquierdo", false},
                {37, "2do molar inferior izquierdo", false},
                {38, "Tercer molar inferior izquierdo", false},
                {41, "Incisivo central inferior derecho", false},
                {42, "Incisivo lateral inferior derecho", false},
                {43, "Canino inferior derecho", false},
                {44, "1er premolar inferior derecho", false},
                {45, "2do premolar inferior derecho", false},
                {46, "1er molar inferior derecho", false},
                {47, "2do molar inferior derecho", false},
                {48, "Tercer molar inferior derecho", false},
        };

        for (Object[] row : teeth) {
            model.addRow(row);
        }

        return model;
    }

    private record SimpleDocumentListener(Runnable action) implements DocumentListener {
        @Override public void insertUpdate(DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(DocumentEvent e) { action.run(); }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        lengthPreviewLabel = new JLabel();
        benefitLabel = new JLabel();
        benefitTextField = new JTextField();
        formatHintLabel = new JLabel();
        toothSelectionScrollPane = new JScrollPane();
        toothSelectionTable = new JTable();
        toothSurfacePanel = new JPanel();
        mesialCheckBox = new JCheckBox();
        distalCheckBox = new JCheckBox();
        vestibularCheckBox = new JCheckBox();
        lingualCheckBox = new JCheckBox();
        occlusalCheckBox = new JCheckBox();
        incisalCheckBox = new JCheckBox();
        palatalCheckBox = new JCheckBox();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
        contentPane.add(lengthPreviewLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- benefitLabel ----
        benefitLabel.setText("Benefit:");
        contentPane.add(benefitLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(benefitTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));
        contentPane.add(formatHintLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== toothSelectionScrollPane ========
        {
            toothSelectionScrollPane.setViewportView(toothSelectionTable);
        }
        contentPane.add(toothSelectionScrollPane, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== toothSurfacePanel ========
        {
            toothSurfacePanel.setBorder(new TitledBorder("Tooth Surface"));
            toothSurfacePanel.setLayout(new GridBagLayout());
            ((GridBagLayout)toothSurfacePanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)toothSurfacePanel.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)toothSurfacePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)toothSurfacePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- mesialCheckBox ----
            mesialCheckBox.setText("Mesial");
            toothSurfacePanel.add(mesialCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- distalCheckBox ----
            distalCheckBox.setText("Distal");
            toothSurfacePanel.add(distalCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- vestibularCheckBox ----
            vestibularCheckBox.setText("Vestibular");
            toothSurfacePanel.add(vestibularCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- lingualCheckBox ----
            lingualCheckBox.setText("Lingual");
            toothSurfacePanel.add(lingualCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- occlusalCheckBox ----
            occlusalCheckBox.setText("Occlusal");
            toothSurfacePanel.add(occlusalCheckBox, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- incisalCheckBox ----
            incisalCheckBox.setText("Incisal");
            toothSurfacePanel.add(incisalCheckBox, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- palatalCheckBox ----
            palatalCheckBox.setText("Palatal");
            toothSurfacePanel.add(palatalCheckBox, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(toothSurfacePanel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel lengthPreviewLabel;
    private JLabel benefitLabel;
    private JTextField benefitTextField;
    private JLabel formatHintLabel;
    private JScrollPane toothSelectionScrollPane;
    private JTable toothSelectionTable;
    private JPanel toothSurfacePanel;
    private JCheckBox mesialCheckBox;
    private JCheckBox distalCheckBox;
    private JCheckBox vestibularCheckBox;
    private JCheckBox lingualCheckBox;
    private JCheckBox occlusalCheckBox;
    private JCheckBox incisalCheckBox;
    private JCheckBox palatalCheckBox;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}