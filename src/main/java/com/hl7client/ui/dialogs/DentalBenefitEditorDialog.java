package com.hl7client.ui.dialogs;

import com.hl7client.model.constants.Hl7Constants;
import com.hl7client.model.dental.*;

import com.hl7client.ui.util.DialogUtils;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
 * Editor para agregar o modificar una prestación odontológica.
 * Soporta dentición permanente (adultos) y temporal (niños),
 * selección opcional de pieza y superficies, y validación estricta.
 */
public class DentalBenefitEditorDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.35;
    private static final double SCREEN_RATIO = 0.45;

    private static final String TOTAL_COUNT = "1";
    private static final String ORIGIN = "P";
    private static final String ITEM_QUANTITY = "1";
    private static final String ASTERISKS = "**";

    private final Consumer<DentalBenefit> onAcceptCallback;
    private DentalBenefit initialBenefit;
    private DentalBenefit result;

    public DentalBenefitEditorDialog(Window owner, Consumer<DentalBenefit> onAccept) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.onAcceptCallback = onAccept;
        initComponents();
        postInit();
        pack();
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);
        setLocationRelativeTo(null);
        installCloseBehavior();
    }

    public void setInitialBenefit(DentalBenefit benefit) {
        this.initialBenefit = benefit;
        if (benefit == null) return;

        // Código (sin prefijo O)
        String code = benefit.getBenefitCode();
        if (code != null && code.startsWith("O") && code.length() > 1) {
            benefitTextField.setText(code.substring(1));
        } else if (code != null) {
            benefitTextField.setText(code);
        }

        // Determinar dentición y seleccionar pieza
        DentalPiece piece = benefit.getPiece();
        if (piece != null) {
            int fdi = Integer.parseInt(piece.getFdiCode());
            DentalToothData.ToothEntry entry = DentalToothData.findByFdi(fdi);
            if (entry != null) {
                boolean isChild = entry.type == DentalPieceType.MOLAR && fdi >= 51 && fdi <= 85;
                denticionComboBox.setSelectedIndex(isChild ? 1 : 0);
                // Forzar actualización de tabla antes de seleccionar
                updateToothTableModel();
                selectPieceInTable(fdi);
            }
        }

        // Superficies
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
        // Inicializar combo de dentición
        denticionComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "Dentición Adulta (permanentes)",
                "Dentición Temporal (niños)"
        }));
        denticionComboBox.setSelectedIndex(0);
        denticionComboBox.addActionListener(e -> {
            updateToothTableModel();
            clearToothSelection();
            updatePreview();
        });

        // Tabla de piezas
        toothSelectionTable.setModel(createToothTableModel(false));
        toothSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer para checkbox
        toothSelectionTable.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            private final JCheckBox renderer = new JCheckBox();
            {
                renderer.setHorizontalAlignment(SwingConstants.CENTER);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                renderer.setSelected(value != null && (Boolean) value);
                renderer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return renderer;
            }
        });

        toothSelectionTable.setDefaultEditor(Object.class, null);

        // Selección exclusiva de pieza
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

        // Listeners de superficies y código
        ActionListener surfaceListener = e -> updatePreview();
        mesialCheckBox.addActionListener(surfaceListener);
        distalCheckBox.addActionListener(surfaceListener);
        vestibularCheckBox.addActionListener(surfaceListener);
        lingualCheckBox.addActionListener(surfaceListener);
        occlusalCheckBox.addActionListener(surfaceListener);
        incisalCheckBox.addActionListener(surfaceListener);
        palatalCheckBox.addActionListener(surfaceListener);

        benefitTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        });

        acceptButton.addActionListener(e -> onAccept());
        updatePreview();
    }

    private void updateToothTableModel() {
        boolean isChild = denticionComboBox.getSelectedIndex() == 1;
        toothSelectionTable.setModel(createToothTableModel(isChild));
    }

    private DefaultTableModel createToothTableModel(boolean child) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"FDI", "Descripción", "Seleccionar"}, 0
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

        List<DentalToothData.ToothEntry> teeth = DentalToothData.getTeeth(child);
        for (DentalToothData.ToothEntry entry : teeth) {
            model.addRow(new Object[]{entry.fdi, entry.description, false});
        }

        return model;
    }

    private void clearToothSelection() {
        for (int row = 0; row < toothSelectionTable.getRowCount(); row++) {
            toothSelectionTable.setValueAt(false, row, 2);
        }
    }

    private void selectPieceInTable(int fdi) {
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

    private void updateTitle() {
        String modo = (initialBenefit != null) ? "Editar" : "Nueva";
        DentalPiece piece = getSelectedPiece();
        String piezaInfo = piece != null ? " – Pieza " + piece.getFdiCode() : "";
        setTitle(modo + " Prestación Odontológica" + piezaInfo);
    }

    private void updatePreview() {
        updateTitle();

        DentalPiece piece = getSelectedPiece();
        Set<DentalSurface> surfaces = getSelectedSurfaces();
        String code = getBenefitCode();
        String hl7 = buildHl7String();
        int len = hl7.length();

        StringBuilder msg = new StringBuilder();
        Color color = new Color(0, 140, 0); // verde por defecto

        if (code.isEmpty()) {
            msg.append("<b>Ingrese código de prestación</b> (ej: 020801)");
            color = new Color(200, 100, 0);
        } else if (piece == null) {
            if (surfaces.isEmpty()) {
                msg.append("<b>Prestación general</b> – Sin pieza ni caras – <b>Válida</b>");
            } else {
                msg.append("<b>¡Atención!</b><br>Superficies ignoradas en HL7 (sin pieza seleccionada)");
                color = new Color(200, 100, 0);
            }
        } else {
            DentalValidationResult validation = DentalSurfaceMatrix.validate(piece, surfaces);
            if (!validation.isValid()) {
                msg.append("<b>Error de combinación:</b><br>").append(validation.getMessage());
                color = Color.RED;
            } else if (len > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
                msg.append("<b>Longitud excede límite:</b> ").append(len)
                        .append(" / ").append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA);
                color = Color.RED;
            } else {
                msg.append("Longitud: <b>").append(len).append(" / ")
                        .append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA).append("</b> caracteres");
                if (!surfaces.isEmpty()) {
                    msg.append("<br>Caras: ").append(surfaces.stream()
                            .map(DentalSurface::name)
                            .sorted()
                            .collect(Collectors.joining(", ")));
                } else {
                    msg.append("<br><i>Sin caras seleccionadas – Válido</i>");
                }
                if (len > Hl7Constants.MAX_LENGTH_ODONTOLOGIA * 0.85) {
                    color = new Color(200, 100, 0);
                }
            }
        }

        lengthPreviewLabel.setText("<html>" + msg + "</html>");
        lengthPreviewLabel.setForeground(color);
        acceptButton.setEnabled(isValidInput());
    }

    private boolean isValidInput() {
        String code = getBenefitCode();
        if (code.isEmpty() || !code.matches("O\\d{5,6}")) {
            return false;
        }

        DentalPiece piece = getSelectedPiece();
        Set<DentalSurface> surfaces = getSelectedSurfaces();

        if (piece != null) {
            DentalValidationResult validation = DentalSurfaceMatrix.validate(piece, surfaces);
            if (!validation.isValid()) return false;
        } else if (!surfaces.isEmpty()) {
            return false;
        }

        return buildHl7String().length() <= Hl7Constants.MAX_LENGTH_ODONTOLOGIA;
    }

    private void onAccept() {
        if (!isValidInput()) {
            StringBuilder msg = new StringBuilder("<html><b>Validación fallida:</b><br><br>");

            String code = getBenefitCode();
            if (code.isEmpty() || !code.matches("O\\d{5,6}")) {
                msg.append("• Código inválido (debe ser O seguido de 5 o 6 dígitos)<br>");
            }

            DentalPiece piece = getSelectedPiece();
            Set<DentalSurface> surfaces = getSelectedSurfaces();

            if (piece != null) {
                DentalValidationResult validation = DentalSurfaceMatrix.validate(piece, surfaces);
                if (!validation.isValid()) {
                    msg.append("• ").append(validation.getMessage().replace("\n", "<br>• ")).append("<br>");
                }
            } else if (!surfaces.isEmpty()) {
                msg.append("• No se pueden seleccionar caras sin pieza dental<br>");
            }

            String hl7 = buildHl7String();
            if (hl7.length() > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
                msg.append("• Longitud excede ").append(Hl7Constants.MAX_LENGTH_ODONTOLOGIA)
                        .append(" caracteres (actual: ").append(hl7.length()).append(")<br>");
            }

            JOptionPane.showMessageDialog(this, msg.append("</html>"),
                    "Validación odontológica", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int fdi = getSelectedFdi();
        DentalPiece piece = (fdi > 0) ? DentalPiece.fromFdi(fdi) : null;
        Set<DentalSurface> surfaces = getSelectedSurfaces();
        String numericCode = benefitTextField.getText().trim();

        result = new DentalBenefit(piece, surfaces, numericCode);

        if (onAcceptCallback != null) {
            onAcceptCallback.accept(result);
        }

        String piezaDesc = (piece != null) ? piece.getFdiCode() : "Ninguna (prestación general)";
        String superficiesDesc = surfaces.isEmpty()
                ? "Ninguna (válido)"
                : surfaces.stream().map(Enum::name).sorted().collect(Collectors.joining(", "));

        dispose();
    }

    // Helpers =================================================================

    private int getSelectedFdi() {
        for (int row = 0; row < toothSelectionTable.getRowCount(); row++) {
            Boolean selected = (Boolean) toothSelectionTable.getValueAt(row, 2);
            if (Boolean.TRUE.equals(selected)) {
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

        // ← Cambio clave: usamos el método helper
        ordered.sort(Comparator.comparingInt(this::getSurfacePriority));

        return ordered.stream()
                .map(DentalSurface::getCode)
                .collect(Collectors.joining());
    }

    private int getSurfacePriority(DentalSurface surface) {
        switch (surface) {
            case VESTIBULAR:  return 0;
            case OCCLUSAL:    return 1;
            case DISTAL:      return 2;
            case MESIAL:      return 3;
            case LINGUAL:     return 4;
            case INCISAL:     return 5;
            case PALATAL:     return 6;
            default:
                // Fallback defensivo (por si agregan nuevas superficies en el futuro)
                return 99;
        }
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

    private void installCloseBehavior() {
        Action cancelAction = DialogUtils.createDisposeAction(this);
        cancelButton.setAction(cancelAction);
        DialogUtils.installCloseAction(this, cancelAction);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        lengthPreviewLabel = new JLabel();
        benefitLabel = new JLabel();
        benefitTextField = new JTextField();
        denticionComboBox = new JComboBox();
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
        Container contentPane = getContentPane();
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
        contentPane.add(denticionComboBox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));
        contentPane.add(formatHintLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
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
    private JComboBox denticionComboBox;
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