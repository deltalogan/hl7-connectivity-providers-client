package com.hl7client.service;

import com.hl7client.client.ApiClient;
import com.hl7client.client.ApiResponse;
import com.hl7client.config.EnvironmentConfig;
import com.hl7client.config.SessionContext;
import com.hl7client.model.dto.request.hl7.*;
import com.hl7client.model.dto.response.hl7.*;
import com.hl7client.model.result.*;
import com.hl7client.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hl7Service {

    private final ApiClient apiClient;

    public Hl7Service(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    // ================== API pública ==================

    public Hl7Result<ElegibilidadResponse> consultarElegibilidad(
            ElegibilidadRequest request
    ) {
        return postHl7(
                EnvironmentConfig.getHl7ElegibilidadUrl(
                        SessionContext.getEnvironment()
                ),
                request,
                ElegibilidadResponse.class,
                this::validarElegibilidad
        );
    }

    public Hl7Result<RegistracionResponse> consultarRegistracion(
            RegistracionRequest request
    ) {
        return postHl7(
                EnvironmentConfig.getHl7RegistracionUrl(
                        SessionContext.getEnvironment()
                ),
                request,
                RegistracionResponse.class,
                this::validarRegistracion
        );
    }

    public Hl7Result<CancelacionResponse> cancelarPrestacion(
            CancelacionRequest request
    ) {
        return postHl7(
                EnvironmentConfig.getHl7CancelacionUrl(
                        SessionContext.getEnvironment()
                ),
                request,
                CancelacionResponse.class,
                this::validarCancelacion
        );
    }

    // ================== Núcleo común ==================

    private <T> Hl7Result<T> postHl7(
            String url,
            Object request,
            Class<T> responseType,
            Hl7Validator<T> validator
    ) {
        if (!SessionContext.isAuthenticated()) {
            return Hl7Result.error(Hl7Error.sessionExpired());
        }

        try {
            String body = JsonUtil.toJson(request);
            ApiResponse response = apiClient.post(url, body, null);

            // 🔒 VALIDACIÓN HTTP (clave)
            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                return Hl7Result.error(
                        Hl7Error.technical(
                                "Error técnico del servidor HL7 (HTTP "
                                        + response.getStatusCode() + ")",
                                Hl7ErrorOrigin.TRANSPORTE
                        )
                );
            }

            if (response.getBody() == null || response.getBody().isBlank()) {
                return errorRespuestaInvalida();
            }

            T hl7 = JsonUtil.fromJson(response.getBody(), responseType);
            return validator.validate(hl7);

        } catch (Exception e) {
            return Hl7Result.error(
                    Hl7Error.technical(
                            "Error técnico procesando respuesta HL7",
                            Hl7ErrorOrigin.PARSEO
                    )
            );
        }
    }

    // ================== Validadores ==================

    private Hl7Result<ElegibilidadResponse> validarElegibilidad(
            ElegibilidadResponse r
    ) {
        if (r == null) {
            return errorRespuestaInvalida();
        }

        if (r.getRechaCabecera() != null && r.getRechaCabecera() > 0) {
            return Hl7Result.rejected(
                    r,
                    Hl7Error.functional(
                            String.valueOf(r.getRechaCabecera()),
                            r.getRechaCabeDeno()
                    )
            );
        }

        return Hl7Result.ok(r);
    }

    private Hl7Result<RegistracionResponse> validarRegistracion(
            RegistracionResponse r
    ) {
        if (r == null || r.getCabecera() == null) {
            return errorRespuestaInvalida();
        }

        RegistracionCabecera cab = r.getCabecera();

        if (cab.getRechaCabecera() != null && cab.getRechaCabecera() > 0) {
            return Hl7Result.rejected(
                    r,
                    Hl7Error.functional(
                            String.valueOf(cab.getRechaCabecera()),
                            cab.getRechaCabeDeno()
                    )
            );
        }

        List<Hl7ItemError> detalles = new ArrayList<>();

        if (r.getDetalle() != null) {
            for (RegistracionDetalle d : r.getDetalle()) {
                if (d.getRecha() != null && d.getRecha() > 0) {
                    detalles.add(
                            new Hl7ItemError(
                                    String.valueOf(d.getRecha()),
                                    d.getDenoItem(),
                                    Hl7ItemErrorOrigin.DETALLE
                            )
                    );
                }
            }
        }

        return detalles.isEmpty()
                ? Hl7Result.ok(r)
                : Hl7Result.partial(r, detalles);
    }

    private Hl7Result<CancelacionResponse> validarCancelacion(
            CancelacionResponse r
    ) {
        if (r == null || r.getCabecera() == null) {
            return errorRespuestaInvalida();
        }

        CancelacionCabecera cab = r.getCabecera();

        if (cab.getRechaCabecera() != null && cab.getRechaCabecera() > 0) {
            return Hl7Result.rejected(
                    r,
                    Hl7Error.functional(
                            String.valueOf(cab.getRechaCabecera()),
                            cab.getRechaCabeDeno()
                    )
            );
        }

        List<Hl7ItemError> detalles = new ArrayList<>();

        if (r.getDetalle() != null) {
            for (CancelacionDetalle d : r.getDetalle()) {
                if (d.getRecha() != null && d.getRecha() > 0) {
                    detalles.add(
                            new Hl7ItemError(
                                    String.valueOf(d.getRecha()),
                                    d.getDenoItem(),
                                    Hl7ItemErrorOrigin.DETALLE
                            )
                    );
                }
            }
        }

        return detalles.isEmpty()
                ? Hl7Result.ok(r)
                : Hl7Result.partial(r, detalles);
    }

    // ================== Helpers ==================

    private <T> Hl7Result<T> errorRespuestaInvalida() {
        return Hl7Result.error(
                Hl7Error.technical(
                        "Respuesta HL7 inválida",
                        Hl7ErrorOrigin.PARSEO
                )
        );
    }

    // ================== Soporte ==================

    @FunctionalInterface
    private interface Hl7Validator<T> {
        Hl7Result<T> validate(T response);
    }
}
