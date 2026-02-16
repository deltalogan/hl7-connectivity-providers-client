package com.hl7client.ui.model;

import com.hl7client.model.Hl7Constants;
import com.hl7client.model.benefit.BenefitItem;
import com.hl7client.model.benefit.MedicalBenefitItem;
import com.hl7client.model.dental.DentalBenefit;
import com.hl7client.model.dental.DentalSurface;
import com.hl7client.model.enums.TipoMensaje;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo de tabla para mostrar prestaciones (médicas y odontológicas) en BenefitDialog.
 * Proporciona columnas claras, tooltips detallados y reglas de negocio para inserción/orden.
 */
public class BenefitTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Cant.", "Chars", "Descripción", "Tipo"};

    private final List<BenefitItem> data = new ArrayList<>();
    private TipoMensaje cachedType = null;

    // =======================
    // TableModel básico
    // =======================
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) return null;
        BenefitItem item = data.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return getQuantityDisplay(item);
            case 1:
                return item.length();
            case 2:
                return getDescriptionDisplay(item);
            case 3:
                return getTypeDisplay(item);
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return Integer.class;
            case 2:
            case 3:
                return String.class;
            default:
                return Object.class;
        }
    }

    // =======================
    // Métodos de visualización
    // =======================
    private int getQuantityDisplay(BenefitItem item) {
        if (item instanceof MedicalBenefitItem) {
            return ((MedicalBenefitItem) item).getQuantityPerType();
        }
        if (item instanceof DentalBenefit) {
            return 1;
        }
        return 0;
    }

    private String getDescriptionDisplay(BenefitItem item) {
        if (item instanceof MedicalBenefitItem) {
            MedicalBenefitItem mbi = (MedicalBenefitItem) item;
            return String.format("%d × %s", mbi.getQuantityPerType(), mbi.getBenefitCode());
        }

        if (item instanceof DentalBenefit) {
            DentalBenefit db = (DentalBenefit) item;
            String pieza = (db.getPiece() != null)
                    ? "Pieza " + db.getPiece().getFdiCode()
                    : "General (sin pieza)";

            String superficies = db.getSurfaces().stream()
                    .map(DentalSurface::getCode)
                    .sorted()
                    .collect(Collectors.joining(""));
            String surfDesc = superficies.isEmpty() ? "sin caras" : superficies;

            return String.format("%s (%s) - %s", pieza, surfDesc, db.getBenefitCode());
        }

        String value = item.getValue();
        if (value == null || value.trim().isEmpty()) return "(sin valor)";
        return value.length() > 80 ? value.substring(0, 77) + "..." : value;
    }

    private String getTypeDisplay(BenefitItem item) {
        if (item instanceof DentalBenefit) return "Odontológica";
        if (item instanceof MedicalBenefitItem) return "Médica";
        return "Desconocido";
    }

    /**
     * Tooltip rico y detallado para la columna "Descripción" (col 2)
     */
    public String getTooltipAt(int row, int column) {
        if (row < 0 || row >= data.size() || column != 2) return null;
        BenefitItem item = data.get(row);

        StringBuilder sb = new StringBuilder("<html><b>Detalle:</b><br>");

        if (item instanceof DentalBenefit) {
            DentalBenefit db = (DentalBenefit) item;
            String pieza = (db.getPiece() != null)
                    ? db.getPiece().getFdiCode() + " (" + db.getPiece().getType().name().toLowerCase() + ")"
                    : "Ninguna (prestación general)";

            String caras = db.getSurfaces().stream()
                    .map(s -> s.name() + " (" + s.getCode() + ")")
                    .sorted()
                    .collect(Collectors.joining(", "));
            caras = caras.isEmpty() ? "Ninguna" : caras;

            sb.append("Pieza: ").append(pieza).append("<br>");
            sb.append("Caras: ").append(caras).append("<br>");
            sb.append("Código: ").append(db.getBenefitCode()).append("<br>");
        } else if (item instanceof MedicalBenefitItem) {
            MedicalBenefitItem mbi = (MedicalBenefitItem) item;
            sb.append("Cantidad: ").append(mbi.getQuantityPerType()).append("<br>");
            sb.append("Código: ").append(mbi.getBenefitCode()).append("<br>");
        }

        sb.append("HL7: <code>").append(item.getValue()).append("</code><br>");
        sb.append("Longitud: <b>").append(item.length()).append("</b> caracteres");

        return sb.append("</html>").toString();
    }

    // =======================
    // API pública (modificación de datos)
    // =======================
    public void add(BenefitItem item) {
        if (item == null) throw new IllegalArgumentException("No se puede agregar un beneficio nulo");
        validateInsert(item);
        data.add(item);
        inferTypeIfNeeded();
        sortData();
        fireTableDataChanged();
    }

    public void remove(int index) {
        if (index >= 0 && index < data.size()) {
            data.remove(index);
            if (data.isEmpty()) cachedType = null;
            sortData();
            fireTableDataChanged();
        }
    }

    public void replace(int index, BenefitItem newItem) {
        if (index >= 0 && index < data.size() && newItem != null) {
            data.set(index, newItem);
            sortData();
            fireTableRowsUpdated(index, index);
        }
    }

    public void setAll(List<? extends BenefitItem> items) {
        data.clear();
        if (items != null && !items.isEmpty()) {
            data.addAll(items);
            inferTypeIfNeeded();
        }
        sortData();
        fireTableDataChanged();
    }

    public BenefitItem get(int index) {
        return (index >= 0 && index < data.size()) ? data.get(index) : null;
    }

    public List<BenefitItem> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(data));  // ← Java 8: en lugar de copyOf
    }

    // =======================
    // Estado y cálculos
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
        return data.stream().anyMatch(b -> b instanceof DentalBenefit);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public boolean isDental() {
        if (cachedType != null) return cachedType == TipoMensaje.ODONTOLOGIA;
        return hasDentalBenefit();
    }

    // =======================
    // Internos
    // =======================
    private void inferTypeIfNeeded() {
        if (cachedType == null && !data.isEmpty()) {
            BenefitItem first = data.get(0);
            if (first instanceof DentalBenefit) {
                cachedType = TipoMensaje.ODONTOLOGIA;
            } else {
                cachedType = TipoMensaje.MEDICINA;
            }
        }
    }

    private void sortData() {
        if (isDental()) {
            data.sort(Comparator.comparingInt(item -> {
                if (item instanceof DentalBenefit) {
                    DentalBenefit db = (DentalBenefit) item;
                    if (db.getPiece() != null) {
                        try {
                            return Integer.parseInt(db.getPiece().getFdiCode());
                        } catch (NumberFormatException e) {
                            return 9999;
                        }
                    }
                }
                return item.getOrden();
            }));
        }
        // Medicina: mantener orden de inserción
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