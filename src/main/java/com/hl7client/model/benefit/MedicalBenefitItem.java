package com.hl7client.model.benefit;

import java.util.Objects;

/**
 * Representa un ítem individual de prestación médica.
 * <p>
 * Formato HL7 generado: {@code cantidad^*código*cantidad**}
 * Ejemplo: {@code 7^*654321*7**}
 * <p>
 * La cantidad se repite al inicio y al final del segmento para cumplir con las reglas HL7 observadas.
 */
public final class MedicalBenefitItem implements BenefitItem {

    private final int quantityPerType;  // Cantidad por este tipo de prestación (1–99)
    private final String benefitCode;   // Código de prestación (exactamente 6 dígitos)
    private final String value;         // Valor completo serializado para HL7

    /**
     * Constructor privado. Usar factory {@link #of(int, String)} en su lugar.
     */
    private MedicalBenefitItem(int quantityPerType, String benefitCode) {
        if (quantityPerType < 1 || quantityPerType > 99) {
            throw new IllegalArgumentException("La cantidad por tipo debe estar entre 1 y 99");
        }
        if (benefitCode == null || benefitCode.trim().length() != 6 || !benefitCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("El código de prestación debe ser exactamente 6 dígitos numéricos: " + benefitCode);
        }

        this.quantityPerType = quantityPerType;
        this.benefitCode = benefitCode.trim();
        this.value = quantityPerType + "^*" + benefitCode + "*" + quantityPerType + "**";
    }

    /**
     * Factory method para crear una instancia.
     *
     * @param quantityPerType cantidad de prestaciones de este tipo (1–99)
     * @param benefitCode     código de la prestación (6 dígitos)
     * @return nueva instancia inmutable
     */
    public static MedicalBenefitItem of(int quantityPerType, String benefitCode) {
        return new MedicalBenefitItem(quantityPerType, benefitCode);
    }

    @Override
    public int getOrden() {
        return 0; // No se usa orden individual en medicina (se calcula total)
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int length() {
        return value.length();
    }

    /**
     * Cantidad asociada a este tipo de prestación.
     */
    public int getQuantityPerType() {
        return quantityPerType;
    }

    /**
     * Código de la prestación (sin prefijos).
     */
    public String getBenefitCode() {
        return benefitCode;
    }

    /**
     * Crea una nueva instancia con la misma prestación pero cantidad diferente.
     * Útil para edición.
     */
    public MedicalBenefitItem withQuantity(int newQuantity) {
        return of(newQuantity, this.benefitCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalBenefitItem that = (MedicalBenefitItem) o;
        return quantityPerType == that.quantityPerType &&
                benefitCode.equals(that.benefitCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantityPerType, benefitCode);
    }

    @Override
    public String toString() {
        return "MedicalBenefitItem{" +
                "quantity=" + quantityPerType +
                ", code='" + benefitCode + '\'' +
                ", hl7='" + value + '\'' +
                '}';
    }
}