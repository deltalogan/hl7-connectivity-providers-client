package com.hl7client.model.benefit;

import com.hl7client.model.Hl7Constants;
import com.hl7client.model.dto.request.hl7.RegistracionRequest;
import com.hl7client.model.dental.DentalBenefit;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper que transforma una lista de {@link BenefitItem} en los campos param1, param2, param3
 * del {@link RegistracionRequest}, respetando estrictamente las reglas HL7 observadas.
 * <ul>
 *   <li><b>Odontología</b>: máximo 1 ítem → valor completo en param1 (ej: "1^*35*V*O020801*P*1**")</li>
 *   <li><b>Medicina</b>: múltiples ítems → total al inicio + separador '|' entre segmentos<br>
 *       Ejemplo: "3^*660001*2**|*660015*1**"</li>
 *   <li><b>Sin prestaciones</b>: param1 = "0^*0*0**", param2 y param3 vacíos</li>
 * </ul>
 * <p>
 * Realiza validaciones defensivas de longitud y consistencia de tipos.
 */
public final class BenefitRequestMapper {

    private static final String EMPTY_PARAM_VALUE = "0^*0*0**";

    private BenefitRequestMapper() {
        // Clase utilitaria estática
    }

    /**
     * Mapea la lista de prestaciones al request HL7.
     * Modifica directamente los campos param1, param2 y param3 del request.
     *
     * @param request  request a modificar (no null)
     * @param benefits lista de prestaciones (puede ser null o vacía)
     * @throws IllegalArgumentException si hay mezcla de tipos, más de 1 odontológica,
     *                                  o si la longitud total excede los límites permitidos
     */
    public static void apply(RegistracionRequest request, List<BenefitItem> benefits) {
        if (request == null) {
            throw new IllegalArgumentException("El request no puede ser null");
        }

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
        request.setParam1(EMPTY_PARAM_VALUE);
        request.setParam2("");
        request.setParam3("");
    }

    private static BenefitType determineType(List<BenefitItem> benefits) {
        boolean hasDental = benefits.stream().anyMatch(b -> b instanceof DentalBenefit);
        boolean hasMedical = benefits.stream().anyMatch(b -> b instanceof MedicalBenefitItem);

        if (hasDental && hasMedical) {
            throw new IllegalArgumentException("No se permiten mezclar prestaciones odontológicas y médicas");
        }

        if (hasDental) {
            if (benefits.size() > 1) {
                throw new IllegalArgumentException(
                        "Solo se permite una prestación odontológica (se encontraron " + benefits.size() + ")");
            }
            return BenefitType.DENTAL;
        }

        return BenefitType.MEDICAL;
    }

    private static void handleDental(RegistracionRequest request, List<BenefitItem> benefits) {
        DentalBenefit item = (DentalBenefit) benefits.get(0);
        String value = item.getValue();

        if (value == null || value.isBlank()) {
            request.setParam1(EMPTY_PARAM_VALUE);
        } else {
            if (value.length() > Hl7Constants.MAX_LENGTH_ODONTOLOGIA) {
                throw new IllegalArgumentException(
                        "Prestación odontológica excede límite de " + Hl7Constants.MAX_LENGTH_ODONTOLOGIA +
                                " caracteres (actual: " + value.length() + ")");
            }
            request.setParam1(value);
        }

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

        int totalQty = items.stream().mapToInt(MedicalBenefitItem::getQuantityPerType).sum();
        if (totalQty == 0) {
            setEmptyParams(request);
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Primer segmento: total^*código*cantidad**
        MedicalBenefitItem first = items.get(0);
        sb.append(totalQty)
                .append("^*")
                .append(first.getBenefitCode())
                .append("*")
                .append(first.getQuantityPerType())
                .append("**");

        // Resto: |*código*cantidad**
        for (int i = 1; i < items.size(); i++) {
            MedicalBenefitItem item = items.get(i);
            sb.append("|*")
                    .append(item.getBenefitCode())
                    .append("*")
                    .append(item.getQuantityPerType())
                    .append("**");
        }

        String fullContent = sb.toString();

        if (fullContent.length() > Hl7Constants.MAX_LENGTH_MEDICINA) {
            throw new IllegalArgumentException(
                    "Prestaciones médicas exceden límite total de " +
                            Hl7Constants.MAX_LENGTH_MEDICINA + " caracteres (actual: " + fullContent.length() + ")");
        }

        List<String> chunks = splitIntoParams(fullContent);

        request.setParam1(chunks.isEmpty() ? EMPTY_PARAM_VALUE : chunks.get(0));
        request.setParam2(chunks.size() > 1 ? chunks.get(1) : "");
        request.setParam3(chunks.size() > 2 ? chunks.get(2) : "");
    }

    /**
     * Divide el contenido en fragmentos de hasta MAX_LENGTH_PER_PARAM caracteres.
     * Intenta no cortar en medio de un segmento (**), pero si no es posible, corta donde corresponda.
     */
    private static List<String> splitIntoParams(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) return chunks;

        int maxLen = Hl7Constants.MAX_LENGTH_PER_PARAM;
        int start = 0;

        while (start < content.length()) {
            int end = Math.min(start + maxLen, content.length());

            // Intentar no cortar en medio de un segmento (buscar el último "**" antes del límite)
            int lastSegmentEnd = content.lastIndexOf("**", end);
            if (lastSegmentEnd >= start && lastSegmentEnd + 2 <= end) {
                end = lastSegmentEnd + 2; // incluir el **
            }  // Si no hay ** cerca, cortar en cualquier punto (el backend parece tolerarlo)


            chunks.add(content.substring(start, end));
            start = end;
        }

        return chunks;
    }

    private enum BenefitType {
        DENTAL, MEDICAL
    }
}