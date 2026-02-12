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
import java.util.stream.Collectors;

/**
 * Diálogo central para gestionar (ABM) las prestaciones (médicas y odontológicas).
 * Permite insertar, editar, eliminar y visualizar la lista de beneficios.
 */
public class BenefitDialog extends JDialog {

    private static final double MINIMUM_SCREEN_RATIO = 0.40;
    private static final double SCREEN_RATIO = 0.50;

    private final TipoMensaje tipoMensaje;
    private final List<BenefitItem> initialBenefits;
    private BenefitTableModel tableModel;

    // NUEVO: flag que indica si el usuario confirmó (Accept) los cambios
    private boolean confirmed = false;

    public BenefitDialog(Window owner, TipoMensaje tipoMensaje, List<BenefitItem> initialBenefits) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.tipoMensaje = tipoMensaje != null ? tipoMensaje : TipoMensaje.MEDICINA;
        this.initialBenefits = List.copyOf(initialBenefits);
        initComponents();
        initLogic();

        pack();

        // Establecemos tamaño mínimo proporcional a la pantalla
        WindowSizer.applyRelativeMinimumSize(this, MINIMUM_SCREEN_RATIO);  // ≈ 22% → ajustable

        // Aplicamos tamaño inicial deseado
        WindowSizer.applyRelativeScreenSize(this, SCREEN_RATIO);

        setLocationRelativeTo(null);
        installCloseBehavior();
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

        // Renderer con tooltips
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
                    "En odontología solo se permite una prestación.\n" +
                            "Edite o elimine la existente antes de agregar otra.",
                    "Límite alcanzado",
                    JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                    "Seleccione un ítem para editar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BenefitItem item = tableModel.get(row);

        if (item instanceof MedicalBenefitItem medicalItem) {
            openMedicalEditor(medicalItem);
        } else if (item instanceof DentalBenefit dentalBenefit) {
            openDentalEditor(dentalBenefit);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Tipo de prestación no editable",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onDelete() {
        int row = benefitTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un ítem para eliminar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BenefitItem item = tableModel.get(row);
        String tipo = (item instanceof DentalBenefit) ? "odontológica" : "médica";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirma eliminar esta prestación " + tipo + "?\n" +
                        "Esta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.remove(row);
            refreshState();
        }
    }

    private void openMedicalEditor(BenefitItem editing) {
        int remaining = tableModel.getRemainingChars(editing);

        MedicalBenefitEditorDialog dialog = new MedicalBenefitEditorDialog(
                this,
                remaining,
                addedItem -> {
                    if (editing == null) {
                        tableModel.add(addedItem);
                    } else {
                        int index = tableModel.getAll().indexOf(editing);
                        if (index >= 0) {
                            tableModel.replace(index, addedItem);
                        }
                    }
                    refreshState();
                }
        );

        if (editing instanceof MedicalBenefitItem mbi) {
            dialog.setInitialValues(mbi.getQuantityPerType(), mbi.getBenefitCode());
        }

        dialog.setVisible(true);
    }

    private void openDentalEditor(DentalBenefit editing) {
        DentalBenefitEditorDialog dialog = new DentalBenefitEditorDialog(
                this,
                dentalResult -> {
                    if (editing != null) {
                        DentalBenefit updated = dentalResult.with(
                                dentalResult.getPiece(),
                                dentalResult.getSurfaces(),
                                dentalResult.getBenefitCode()
                        );
                        int index = tableModel.getAll().indexOf(editing);
                        if (index >= 0) {
                            tableModel.replace(index, updated);
                        }
                    } else {
                        tableModel.setAll(List.of(dentalResult));
                    }

                    refreshState();

                    // Manejar pieza null en el mensaje
                    String piezaDesc = (dentalResult.getPiece() != null)
                            ? dentalResult.getPiece().getFdiCode()
                            : "Ninguna";

                    String superficies = dentalResult.getSurfaces().stream()
                            .map(DentalSurface::name)
                            .collect(Collectors.joining(", "));
                    superficies = superficies.isEmpty() ? "Ninguna" : superficies;

                    String accion = editing != null ? "actualizada" : "agregada";
                    JOptionPane.showMessageDialog(this,
                            "Prestación odontológica " + accion + " correctamente.\n\n" +
                                    "Pieza: " + piezaDesc + "\n" +
                                    "Superficies: " + superficies + "\n" +
                                    "Código: " + dentalResult.getBenefitCode() + "\n" +
                                    "Longitud: " + dentalResult.length() + " caracteres",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                }
        );

        if (editing != null) {
            dialog.setInitialBenefit(editing);
        }

        dialog.setVisible(true);
    }

    private void onFinalAccept() {
        int totalChars = tableModel.getTotalChars();
        int maxChars = (tipoMensaje == TipoMensaje.ODONTOLOGIA)
                ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA
                : Hl7Constants.MAX_LENGTH_MEDICINA;

        if (totalChars > maxChars) {
            JOptionPane.showMessageDialog(this,
                    "La longitud total (" + totalChars + ") excede el máximo permitido (" + maxChars + ").\n" +
                            "Elimine o edite ítems antes de continuar.",
                    "Límite excedido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tableModel.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "No hay prestaciones cargadas.\n" +
                            "¿Confirma cerrar sin agregar ninguna prestación?",
                    "Sin prestaciones",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // NUEVO: ¡Aquí marcamos que el usuario confirmó los cambios!
        this.confirmed = true;
        dispose();
    }

    private void refreshState() {
        int selectedRow = benefitTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;

        // Habilitar/deshabilitar botones
        boolean canInsert = (tipoMensaje != TipoMensaje.ODONTOLOGIA) || !tableModel.hasDentalBenefit();
        insertButton.setEnabled(canInsert);
        insertButton.setToolTipText(canInsert ? "Agregar nueva prestación" : "Límite alcanzado (solo 1 en odontología)");

        updateButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);

        // Estado de caracteres
        int total = tableModel.getTotalChars();
        int max = (tipoMensaje == TipoMensaje.ODONTOLOGIA)
                ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA
                : Hl7Constants.MAX_LENGTH_MEDICINA;

        int count = tableModel.getRowCount();
        String tipoDesc = (tipoMensaje == TipoMensaje.ODONTOLOGIA) ? "Odontología" : "Medicina";

        // Título informativo
        String title = String.format("Gestión de Prestaciones - %s (%d %s - %d / %d caracteres)",
                tipoDesc,
                count,
                count == 1 ? "prestación" : "prestaciones",
                total,
                max);

        Color bgColor = UIManager.getColor("Panel.background");

        if (total > max) {
            title += " - ¡Excedido!";
            bgColor = new Color(255, 220, 220); // rojo claro
        } else if (total > max * 0.9) {
            title += " - ¡Muy cerca!";
            bgColor = new Color(255, 235, 200); // naranja claro
        } else if (total > max * 0.75) {
            title += " - Acercándose";
            bgColor = new Color(255, 245, 200); // ámbar suave
        }

        setTitle(title);
        getContentPane().setBackground(bgColor);

        // Actualizar summaryLabel
        if (summaryLabel != null) {
            String summaryText;
            if (tableModel.isEmpty()) {
                summaryText = "No hay prestaciones cargadas aún";
            } else {
                summaryText = String.format("%d %s cargada%s • %d / %d caracteres usados",
                        count,
                        count == 1 ? "prestación" : "prestaciones",
                        count == 1 ? "" : "s",
                        total,
                        max);
            }

            summaryLabel.setText(summaryText);
            summaryLabel.setForeground(total > max ? Color.RED : new Color(60, 60, 60)); // gris oscuro
        }
    }

    /**
     * Indica si el usuario confirmó (presionó Accept) los cambios realizados en este diálogo.
     * @return true si se presionó Accept y se pasaron todas las validaciones, false si se canceló o cerró de otra forma
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Devuelve la lista actual de beneficios.
     * Si el usuario NO confirmó (canceló), devuelve la lista inicial (sin modificaciones).
     */
    public List<BenefitItem> getBenefits() {
        if (confirmed) {
            return tableModel.getAll();
        } else {
            // Si canceló, devolvemos los valores originales para no "contaminar" la lista del llamador
            return List.copyOf(initialBenefits);
        }
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