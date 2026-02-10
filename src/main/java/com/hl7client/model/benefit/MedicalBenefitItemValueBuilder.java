package com.hl7client.model.benefit;

/**
 * Clase auxiliar para construir el valor HL7 de un MedicalBenefitItem
 * de manera consistente y con menos riesgo de errores de formato.
 */
public final class MedicalBenefitItemValueBuilder {

    private MedicalBenefitItemValueBuilder() {
        // Clase estática
    }

    /**
     * Construye el valor HL7 para un ítem médico individual
     * Formato: cantidad^*código*cantidad**
     */
    public static String buildIndividual(int quantity, String benefitCode) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Cantidad debe estar entre 1 y 99");
        }
        if (benefitCode == null || !benefitCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("Código de prestación debe ser 6 dígitos numéricos");
        }
        return quantity + "^*" + benefitCode + "*" + quantity + "**";
    }

    /**
     * Construye el valor HL7 para el primer ítem del grupo (con cantidad total)
     * Formato: total^*código*cantidad_del_primer_item**
     */
    public static String buildAsFirstItem(int totalQuantity, String benefitCode, int itemQuantity) {
        if (totalQuantity < 1) {
            throw new IllegalArgumentException("Cantidad total debe ser mayor a 0");
        }
        if (benefitCode == null || !benefitCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("Código de prestación debe ser 6 dígitos numéricos");
        }
        return totalQuantity + "^*" + benefitCode + "*" + itemQuantity + "**";
    }

    /**
     * Construye el valor HL7 para un ítem secundario
     * Formato: *código*cantidad**
     */
    public static String buildAsSecondaryItem(String benefitCode, int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Cantidad debe estar entre 1 y 99");
        }
        if (benefitCode == null || !benefitCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("Código de prestación debe ser 6 dígitos numéricos");
        }
        return "*" + benefitCode + "*" + quantity + "**";
    }
}