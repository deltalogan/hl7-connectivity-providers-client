package com.hl7client.ui.dialogs;

import com.hl7client.model.Hl7Constants;
import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.MedicalBenefitItem;
import com.hl7client.model.dental.DentalBenefit;
import com.hl7client.model.dental.DentalSurface;
import com.hl7client.model.enums.TipoMensaje;
import com.hl7client.ui.model.BenefitTableModel;
import com.hl7client.ui.util.DialogUtils;
import com.hl7client.ui.util.WindowSizer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Diálogo central para gestionar (ABM) las prestaciones médicas y odontológicas.
 * Permite insertar, editar, eliminar y confirmar la lista de beneficios.
 * Soporta modo cancelación (prestaciones solo con códigos, cantidad fija = 1 en medicina).
 */
public class BenefitDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.55; // un poco más alto para mejor visibilidad

    private static final int MAX_LENGTH_CANCEL_MEDICINA = 255 * 2; // 510 para param1 + param2

    private final TipoMensaje tipoMensaje;
    private final List<BenefitItem> initialBenefits;
    private final boolean isCancelMode;

    private BenefitTableModel tableModel;
    private boolean confirmed = false;

    public BenefitDialog(Window owner, TipoMensaje tipoMensaje, List<BenefitItem> initialBenefits, boolean isCancelMode) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.tipoMensaje = tipoMensaje != null ? tipoMensaje : TipoMensaje.MEDICINA;
        this.initialBenefits = List.copyOf(initialBenefits);
        this.isCancelMode = isCancelMode;

        initComponents();
        initLogic();
        pack();
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);
        setLocationRelativeTo(null);
        installCloseBehavior();
    }

    // Sobrecarga para mantener compatibilidad con código existente
    public BenefitDialog(Window owner, TipoMensaje tipoMensaje, List<BenefitItem> initialBenefits) {
        this(owner, tipoMensaje, initialBenefits, false);
    }

    private void initLogic() {
        tableModel = new BenefitTableModel();
        benefitTable.setModel(tableModel);
        benefitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (!initialBenefits.isEmpty()) {
            tableModel.setAll(initialBenefits);
        }

        // Configuración visual de columnas
        benefitTable.getColumnModel().getColumn(0).setPreferredWidth(90);   // Cantidad
        benefitTable.getColumnModel().getColumn(0).setMaxWidth(110);
        benefitTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {{
            setHorizontalAlignment(SwingConstants.CENTER);
        }});

        benefitTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Chars
        benefitTable.getColumnModel().getColumn(1).setMaxWidth(100);
        benefitTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {{
            setHorizontalAlignment(SwingConstants.CENTER);
        }});

        benefitTable.getColumnModel().getColumn(2).setPreferredWidth(500);  // Contenido
        benefitTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Tipo

        // Renderer con tooltips en columna Contenido
        benefitTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row >= 0 && column == 2) {
                    String tooltip = tableModel.getTooltipAt(row, column);
                    ((JComponent) c).setToolTipText(tooltip);
                } else {
                    ((JComponent) c).setToolTipText(null);
                }
                return c;
            }
        });

        // Listeners
        insertButton.addActionListener(e -> onInsert());
        updateButton.addActionListener(e -> onUpdate());
        deleteButton.addActionListener(e -> onDelete());
        acceptButton.addActionListener(e -> onFinalAccept());
        cancelButton.addActionListener(e -> dispose());

        benefitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshState();
            }
        });

        refreshState();
    }

    private void onInsert() {
        if (tipoMensaje == TipoMensaje.ODONTOLOGIA && tableModel.hasDentalBenefit()) {
            JOptionPane.showMessageDialog(this,
                    "En odontología solo se permite una prestación.\nEdite o elimine la existente primero.",
                    "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tipoMensaje == TipoMensaje.ODONTOLOGIA) {
            openDentalEditor(null);
        } else {
            openMedicalEditor(null);
        }
    }

    private void onUpdate() {
        int row = benefitTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem para editar", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BenefitItem item = tableModel.get(row);
        if (item instanceof MedicalBenefitItem medical) {
            openMedicalEditor(medical);
        } else if (item instanceof DentalBenefit dental) {
            openDentalEditor(dental);
        } else {
            JOptionPane.showMessageDialog(this, "Tipo de prestación no editable", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onDelete() {
        int row = benefitTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem para eliminar", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BenefitItem item = tableModel.get(row);
        String tipo = (item instanceof DentalBenefit) ? "odontológica" : "médica";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirma eliminar esta prestación " + tipo + "?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.remove(row);
            refreshState();
        }
    }

    private void openMedicalEditor(BenefitItem editing) {
        int remaining = tableModel.getRemainingChars(editing);

        JDialog editorDialog;
        Consumer<BenefitItem> callback = added -> {
            if (editing == null) {
                tableModel.add(added);
            } else {
                int idx = tableModel.getAll().indexOf(editing);
                if (idx >= 0) tableModel.replace(idx, added);
            }
            refreshState();
        };

        if (isCancelMode) {
            editorDialog = new CancelMedicalBenefitEditorDialog(this, remaining, callback);
        } else {
            editorDialog = new MedicalBenefitEditorDialog(this, remaining, callback);
        }

        if (editing instanceof MedicalBenefitItem mbi) {
            if (editorDialog instanceof MedicalBenefitEditorDialog normalEditor) {
                normalEditor.setInitialValues(mbi.getQuantityPerType(), mbi.getBenefitCode());
            }
            // En modo cancelación no seteamos valores iniciales de cantidad (siempre 1)
        }

        editorDialog.setVisible(true);
    }

    private void openDentalEditor(DentalBenefit editing) {
        DentalBenefitEditorDialog dialog = new DentalBenefitEditorDialog(this, dentalResult -> {
            if (editing != null) {
                int idx = tableModel.getAll().indexOf(editing);
                if (idx >= 0) {
                    tableModel.replace(idx, dentalResult);
                }
            } else {
                tableModel.setAll(List.of(dentalResult));
            }
            refreshState();

            String accion = editing != null ? "actualizada" : "agregada";
            String piezaDesc = (dentalResult.getPiece() != null)
                    ? dentalResult.getPiece().getFdiCode()
                    : "Ninguna (prestación general)";

            String superficiesDesc = dentalResult.getSurfaces().isEmpty()
                    ? "Ninguna"
                    : dentalResult.getSurfaces().stream()
                    .map(DentalSurface::name)
                    .sorted()
                    .collect(Collectors.joining(", "));

            JOptionPane.showMessageDialog(this,
                    "<html><b>Prestación odontológica " + accion + " correctamente</b><br><br>" +
                            "Pieza: <b>" + piezaDesc + "</b><br>" +
                            "Superficies: <b>" + superficiesDesc + "</b><br>" +
                            "Código: <b>" + dentalResult.getBenefitCode() + "</b><br>" +
                            "Longitud HL7: <b>" + dentalResult.length() + "</b> caracteres</html>",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        if (editing != null) {
            dialog.setInitialBenefit(editing);
        }
        dialog.setVisible(true);
    }

    private void onFinalAccept() {
        int total = tableModel.getTotalChars();
        int max = getMaxLength();

        if (total > max) {
            JOptionPane.showMessageDialog(this,
                    "Longitud total (" + total + ") excede el máximo permitido (" + max + ").\n" +
                            "Elimine o edite ítems antes de continuar.",
                    "Límite excedido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tableModel.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "No hay prestaciones cargadas.\n¿Confirma cerrar sin agregar ninguna?",
                    "Sin prestaciones", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        confirmed = true;
        dispose();
    }

    private int getMaxLength() {
        if (isCancelMode && tipoMensaje != TipoMensaje.ODONTOLOGIA) {
            return MAX_LENGTH_CANCEL_MEDICINA; // 510
        }
        return (tipoMensaje == TipoMensaje.ODONTOLOGIA)
                ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA
                : Hl7Constants.MAX_LENGTH_MEDICINA;
    }

    private void refreshState() {
        boolean hasSelection = benefitTable.getSelectedRow() >= 0;

        // Botones
        boolean canInsert = (tipoMensaje != TipoMensaje.ODONTOLOGIA) || !tableModel.hasDentalBenefit();
        insertButton.setEnabled(canInsert);
        insertButton.setToolTipText(canInsert ? "Agregar nueva prestación" : "Límite alcanzado (solo 1 en odontología)");

        updateButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);

        // Estado de caracteres
        int used = tableModel.getTotalChars();
        int max = getMaxLength();  // ← límite correcto según modo
        int remaining = max - used;
        int count = tableModel.getRowCount();

        String tipoDesc = (tipoMensaje == TipoMensaje.ODONTOLOGIA) ? "Odontología" : "Medicina";
        String modePrefix = isCancelMode ? "Cancelación – " : "";
        String countText = count + " " + (count == 1 ? "prestación" : "prestaciones");

        // Título dinámico con límite real
        String title = String.format(
                "Gestión de Prestaciones - %s%s • %s • %d / %d chars (restan %d)",
                modePrefix, tipoDesc, countText, used, max, remaining
        );

        Color bgColor = UIManager.getColor("Panel.background");

        // Solo alertas cambian el fondo (respeta Dark Mode)
        if (remaining < 0) {
            title += " → ¡Excedido!";
            bgColor = new Color(255, 220, 220);
        } else if (remaining < 50) {
            title += " → ¡Muy poco!";
            bgColor = new Color(255, 235, 180);
        } else if (remaining < 150) {
            title += " → Cerca del límite";
            bgColor = new Color(255, 245, 200);
        }

        setTitle(title);

        // Aplicamos fondo SOLO en alertas
        if (remaining < 150) {
            getContentPane().setBackground(bgColor);
        } else {
            getContentPane().setBackground(UIManager.getColor("Panel.background"));
        }

        // Summary label
        if (summaryLabel != null) {
            String summary = tableModel.isEmpty()
                    ? "No hay prestaciones cargadas aún"
                    : String.format("%d %s • %d / %d chars usados (restan %d)",
                    count, count == 1 ? "prestación" : "prestaciones", used, max, remaining);

            summaryLabel.setText(summary);
            summaryLabel.setForeground(remaining < 0 ? Color.RED :
                    remaining < 100 ? new Color(200, 100, 0) : new Color(60, 60, 60));
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<BenefitItem> getBenefits() {
        return confirmed ? tableModel.getAll() : List.copyOf(initialBenefits);
    }

    private void installCloseBehavior() {
        Action cancelAction = DialogUtils.createDisposeAction(this);
        cancelButton.setAction(cancelAction);
        DialogUtils.installCloseAction(this, cancelAction);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        summaryLabel = new JLabel();
        insertButton = new JButton();
        benefitScrollPane = new JScrollPane();
        benefitTable = new JTable();
        updateButton = new JButton();
        deleteButton = new JButton();
        acceptButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
        contentPane.add(summaryLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- insertButton ----
        insertButton.setText("Insert");
        contentPane.add(insertButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== benefitScrollPane ========
        {
            benefitScrollPane.setViewportView(benefitTable);
        }
        contentPane.add(benefitScrollPane, new GridBagConstraints(0, 1, 1, 3, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- updateButton ----
        updateButton.setText("Update");
        contentPane.add(updateButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- deleteButton ----
        deleteButton.setText("Delete");
        contentPane.add(deleteButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- acceptButton ----
        acceptButton.setText("Accept");
        contentPane.add(acceptButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel summaryLabel;
    private JButton insertButton;
    private JScrollPane benefitScrollPane;
    private JTable benefitTable;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton acceptButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}