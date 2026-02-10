package com.hl7client.model.benefit;

import com.hl7client.model.Hl7Constants;
import com.hl7client.model.dto.request.hl7.RegistracionRequest;
import com.hl7client.model.dental.DentalBenefit;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper responsable de transformar una lista de BenefitItem en los campos param1, param2, param3
 * del RegistracionRequest, respetando las reglas HL7 específicas para odontología y medicina.
 * <ul>
 *   <li><b>Odontología</b>: máximo 1 ítem → se coloca completo en param1 (ej: "1^*35*V*O020801*P*1**")</li>
 *   <li><b>Medicina</b>: múltiples ítems → cantidad total al inicio, separador '|' entre ítems<br>
 *       Formato: <code>total^*código*cantidad**|*código*cantidad**|...</code></li>
 *   <li><b>Sin prestaciones</b>: param1 = "0^*0*0**", param2 y param3 vacíos (requerido por el endpoint)</li>
 * </ul>
 */
public final class BenefitRequestMapper {

    private static final String EMPTY_PARAM1_VALUE = "0^*0*0**";

    private BenefitRequestMapper() {
        // Clase estática
    }

    /**
     * Aplica el mapeo de la lista de benefits al RegistracionRequest.
     *
     * @param request  el request a modificar
     * @param benefits lista de prestaciones (puede ser null o vacía)
     * @throws IllegalArgumentException si se detecta mezcla de tipos, más de 1 odontología,
     *                                  o si la longitud total excede el límite permitido
     */
    public static void apply(RegistracionRequest request, List<BenefitItem> benefits) {
        if (benefits == null || benefits.isEmpty()) {
            setEmptyParams(request);
            return;
        }

        BenefitType type = determineType(benefits);

        switch (type) {
            case DENTAL -> handleDental(request, benefits);
            case MEDICAL -> handleMedical(request, benefits);
        }
    }

    private static void setEmptyParams(RegistracionRequest request) {
        request.setParam1(EMPTY_PARAM1_VALUE);
        request.setParam2("");
        request.setParam3("");
    }

    private static BenefitType determineType(List<BenefitItem> benefits) {
        boolean hasDental = benefits.stream().anyMatch(b -> b instanceof DentalBenefit);
        boolean hasMedical = benefits.stream().anyMatch(b -> b instanceof MedicalBenefitItem);

        if (hasDental && hasMedical) {
            throw new IllegalArgumentException(
                    "No se permiten mezclar prestaciones odontológicas y médicas en la misma registración");
        }

        if (hasDental) {
            if (benefits.size() > 1) {
                throw new IllegalArgumentException(
                        "Solo se permite una prestación odontológica por registración (se encontraron " + benefits.size() + ")");
            }
            return BenefitType.DENTAL;
        }

        return BenefitType.MEDICAL;
    }

    private static void handleDental(RegistracionRequest request, List<BenefitItem> benefits) {
        BenefitItem item = benefits.get(0);
        String value = item.getValue();

        // Validación de longitud (defensa extra, aunque la UI ya debería controlarlo)
        if (value != null && value.length() > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
            throw new IllegalArgumentException(
                    "Prestación odontológica excede el límite de " + Hl7Constants.MAX_LENGTH_ODONTOLOGIA +
                            " caracteres (longitud actual: " + value.length() + ")");
        }

        request.setParam1(value != null && !value.isBlank() ? value : EMPTY_PARAM1_VALUE);
        request.setParam2("");
        request.setParam3("");
    }

    private static void handleMedical(RegistracionRequest request, List<BenefitItem> benefits) {
        List<MedicalBenefitItem> items = benefits.stream()
                .filter(b -> b instanceof MedicalBenefitItem)
                .map(b -> (MedicalBenefitItem) b)
                .toList();

        if (items.isEmpty()) {
            setEmptyParams(request);
            return;
        }

        int totalQuantity = items.stream()
                .mapToInt(MedicalBenefitItem::getQuantityPerType)
                .sum();

        if (totalQuantity == 0) {
            setEmptyParams(request);
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Primer segmento: total ^ * código1 * cantidad1 **
        MedicalBenefitItem first = items.get(0);
        sb.append(totalQuantity)
                .append("^")
                .append("*")
                .append(first.getBenefitCode())
                .append("*")
                .append(first.getQuantityPerType())
                .append("**");

        // Ítems siguientes: | * código * cantidad **
        for (int i = 1; i < items.size(); i++) {
            MedicalBenefitItem item = items.get(i);
            sb.append("|")
                    .append("*")
                    .append(item.getBenefitCode())
                    .append("*")
                    .append(item.getQuantityPerType())
                    .append("**");
        }

        String fullContent = sb.toString();

        // Validación de longitud total (defensa extra)
        if (fullContent.length() > Hl7Constants.MAX_LENGTH_MEDICINA) {
            throw new IllegalArgumentException(
                    "Las prestaciones médicas exceden el límite total permitido de " +
                            Hl7Constants.MAX_LENGTH_MEDICINA + " caracteres (actual: " + fullContent.length() + ")");
        }

        List<String> chunks = chunkContent(fullContent);

        request.setParam1(chunks.isEmpty() ? EMPTY_PARAM1_VALUE : chunks.get(0));
        request.setParam2(chunks.size() > 1 ? chunks.get(1) : "");
        request.setParam3(chunks.size() > 2 ? chunks.get(2) : "");
    }

    /**
     * Divide el contenido en fragmentos de longitud máxima.
     * <p>
     * Nota: corta en cualquier punto (el endpoint actual parece tolerarlo).
     * Si en el futuro se requiere no cortar en medio de un segmento (respetando '|'),
     * se puede implementar una versión más inteligente.
     */
    private static List<String> chunkContent(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        int index = 0;
        while (index < content.length()) {
            int end = Math.min(index + Hl7Constants.MAX_LENGTH_PER_PARAM, content.length());
            chunks.add(content.substring(index, end));
            index = end;
        }
        return chunks;
    }

    /**
     * Enum interno para clasificar el tipo de benefits.
     */
    private enum BenefitType {
        DENTAL, MEDICAL
    }
}