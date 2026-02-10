package com.hl7client.ui.model;

import com.hl7client.model.Hl7Constants;
import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.MedicalBenefitItem;
import com.hl7client.model.dental.DentalBenefit;
import com.hl7client.model.dental.DentalSurface;
import com.hl7client.model.enums.TipoMensaje;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TableModel para la tabla de beneficios en BenefitDialog.
 * Muestra información clara y útil para el usuario.
 * Soporta DentalBenefit con pieza null.
 */
public class BenefitTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Cantidad", "Chars", "Contenido", "Tipo"};

    private final List<BenefitItem> data = new ArrayList<>();

    private TipoMensaje cachedTipo = null;

    // =======================
    // TableModel básico
    // =======================
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) return null;

        BenefitItem item = data.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> getCantidad(item);
            case 1 -> item.length();
            case 2 -> getContenidoLegible(item);
            case 3 -> getTipoDescripcion(item);
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1 -> Integer.class;
            case 2, 3 -> String.class;
            default -> Object.class;
        };
    }

    // =======================
    // Métodos de datos
    // =======================
    private int getCantidad(BenefitItem item) {
        if (item == null) return 0;
        if (item instanceof MedicalBenefitItem mbi) return mbi.getQuantityPerType();
        if (item instanceof DentalBenefit) return 1;
        return 0;
    }

    private String getContenidoLegible(BenefitItem item) {
        if (item instanceof MedicalBenefitItem mbi) {
            return String.format("%d × %s", mbi.getQuantityPerType(), mbi.getBenefitCode());
        }
        if (item instanceof DentalBenefit db) {
            String surfaces = db.getSurfaces().stream()
                    .map(DentalSurface::getCode)
                    .collect(Collectors.joining(""));
            String surfDesc = surfaces.isEmpty() ? "sin caras" : surfaces;

            String piezaDesc = (db.getPiece() != null)
                    ? "Pieza " + db.getPiece().getFdiCode()
                    : "Sin pieza";

            return String.format("%s (%s) - %s",
                    piezaDesc, surfDesc, db.getBenefitCode());
        }

        String value = item.getValue();
        return (value != null && !value.isBlank())
                ? (value.length() > 60 ? value.substring(0, 57) + "..." : value)
                : "(sin valor)";
    }

    private String getTipoDescripcion(BenefitItem item) {
        if (item instanceof DentalBenefit) return "Odontológica";
        if (item instanceof MedicalBenefitItem) return "Médica";
        return "Desconocido";
    }

    /**
     * Tooltip detallado y formateado para la columna "Contenido"
     */
    public String getTooltipAt(int row, int column) {
        if (row < 0 || row >= data.size() || column != 2) return null;

        BenefitItem item = data.get(row);

        if (item instanceof DentalBenefit db) {
            String surfaces = db.getSurfaces().stream()
                    .map(Enum::name)
                    .sorted()
                    .collect(Collectors.joining(", "));

            String piezaTooltip = (db.getPiece() != null)
                    ? db.getPiece().getFdiCode()
                    : "Ninguna";

            return String.format("<html><b>Pieza:</b> %s<br>" +
                            "<b>Caras:</b> %s<br>" +
                            "<b>Código:</b> %s<br>" +
                            "<b>HL7:</b> %s<br>" +
                            "<b>Longitud:</b> %d caracteres</html>",
                    piezaTooltip,
                    surfaces.isEmpty() ? "Ninguna" : surfaces,
                    db.getBenefitCode(),
                    db.getValue(),
                    db.length());
        }

        if (item instanceof MedicalBenefitItem mbi) {
            return String.format("<html><b>Cantidad:</b> %d<br>" +
                            "<b>Código:</b> %s<br>" +
                            "<b>HL7:</b> %s<br>" +
                            "<b>Longitud:</b> %d caracteres</html>",
                    mbi.getQuantityPerType(),
                    mbi.getBenefitCode(),
                    mbi.getValue(),
                    mbi.length());
        }

        String value = item.getValue();
        return value != null
                ? String.format("<html><b>HL7:</b> %s<br><b>Longitud:</b> %d caracteres</html>",
                value, item.length())
                : "Sin datos";
    }

    // =======================
    // API pública
    // =======================
    public void add(BenefitItem item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede agregar un beneficio nulo");
        }
        validateInsert(item);
        data.add(item);
        inferTipoIfNeeded();
        sort();
        fireTableDataChanged();
    }

    public void remove(int index) {
        if (index >= 0 && index < data.size()) {
            data.remove(index);
            if (data.isEmpty()) cachedTipo = null;
            sort();
            fireTableDataChanged();
        }
    }

    public void replace(int index, BenefitItem newItem) {
        if (index >= 0 && index < data.size() && newItem != null) {
            data.set(index, newItem);
            sort();
            fireTableRowsUpdated(index, index);
        }
    }

    public void setAll(List<? extends BenefitItem> items) {
        data.clear();
        if (items != null && !items.isEmpty()) {
            data.addAll(items);
            inferTipoIfNeeded();
        }
        sort();
        fireTableDataChanged();
    }

    public BenefitItem get(int index) {
        return (index >= 0 && index < data.size()) ? data.get(index) : null;
    }

    public List<BenefitItem> getAll() {
        return List.copyOf(data);
    }

    // =======================
    // Reglas de negocio y estado
    // =======================
    public int getTotalChars() {
        return data.stream().mapToInt(BenefitItem::length).sum();
    }

    public int getRemainingChars(BenefitItem editing) {
        int used = data.stream()
                .filter(b -> b != editing)
                .mapToInt(BenefitItem::length)
                .sum();
        return getMaxChars() - used;
    }

    public int getMaxChars() {
        return isDental() ? Hl7Constants.MAX_LENGTH_ODONTOLOGIA : Hl7Constants.MAX_LENGTH_MEDICINA;
    }

    public boolean hasDentalBenefit() {
        return data.stream().anyMatch(DentalBenefit.class::isInstance);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public boolean isDental() {
        if (cachedTipo != null) return cachedTipo == TipoMensaje.ODONTOLOGIA;
        return hasDentalBenefit();
    }

    // =======================
    // Métodos internos
    // =======================
    private void inferTipoIfNeeded() {
        if (cachedTipo == null && !data.isEmpty()) {
            cachedTipo = data.get(0) instanceof DentalBenefit
                    ? TipoMensaje.ODONTOLOGIA
                    : TipoMensaje.MEDICINA;
        }
    }

    private void sort() {
        if (isDental()) {
            data.sort(Comparator.comparingInt(BenefitItem::getOrden));
        }
        // En medicina mantenemos orden de inserción
    }

    private void validateInsert(BenefitItem item) {
        boolean isDentalItem = item instanceof DentalBenefit;

        if (isDentalItem) {
            if (!data.isEmpty() && !isDental()) {
                throw new IllegalStateException("No se pueden mezclar prestaciones odontológicas y médicas");
            }
            if (hasDentalBenefit()) {
                throw new IllegalStateException("Solo se permite una prestación odontológica");
            }
        } else {
            if (hasDentalBenefit()) {
                throw new IllegalStateException("No se pueden agregar prestaciones médicas si ya existe una odontológica");
            }
        }

        if (isDentalItem && data.stream().anyMatch(b -> b.getOrden() == item.getOrden())) {
            throw new IllegalArgumentException("Ya existe un beneficio odontológico con orden " + item.getOrden());
        }
    }
}