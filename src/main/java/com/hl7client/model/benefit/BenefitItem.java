package com.hl7client.model.benefit;

/**
 * Contrato base para cualquier beneficio (médico u odontológico).
 *
 * Representa una unidad lógica serializable a HL7,
 * independiente de UI y de la capa de transporte.
 */
public interface BenefitItem {

    /**
     * Orden lógico del beneficio dentro del mensaje (1..n).
     * Define el orden de serialización HL7.
     */
    int getOrden();

    /**
     * Longitud lógica del beneficio en caracteres,
     * utilizada para control de límites (255 * n).
     */
    int length();

    /**
     * Valor textual del beneficio ya formateado para HL7.
     * No incluye el nombre del parámetro (paramX).
     */
    String getValue();
}
