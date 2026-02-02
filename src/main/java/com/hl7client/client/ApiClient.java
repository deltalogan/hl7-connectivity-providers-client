package com.hl7client.client;

import com.hl7client.config.SessionContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiClient {

    private static final Logger LOGGER =
            Logger.getLogger(ApiClient.class.getName());

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient client;
    private final AuthRefresher authRefresher;

    public ApiClient(AuthRefresher authRefresher) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.authRefresher = Objects.requireNonNull(authRefresher);
    }

    public ApiResponse post(String url, String body, Map<String, String> headers) {
        return postInternal(url, body, headers, true);
    }

    private ApiResponse postInternal(
            String url,
            String body,
            Map<String, String> headers,
            boolean allowRetry
    ) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body));

            buildHeaders(headers).forEach(builder::header);

            HttpRequest request = builder.build();

            // 🔍 Logs de debugging
            logRequest(request, body);
            logAsCurl(request, body);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            logResponse(response);

            // 🔐 Refresh automático
            if (response.statusCode() == 401 && allowRetry && canRefresh(url)) {
                LOGGER.info("401 received, attempting auth refresh");
                authRefresher.refreshAuth();
                return postInternal(url, body, headers, false);
            }

            return new ApiResponse(
                    response.statusCode(),
                    response.body(),
                    Map.of()
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Transport error calling API", e);
            throw new RuntimeException("Error de comunicación con el servicio", e);
        }
    }

    // ================== Logging ==================

    private void logRequest(HttpRequest request, String body) {
        System.out.println("=== REQUEST ===");
        System.out.println("URL: " + request.uri());
        System.out.println("Method: " + request.method());
        System.out.println("Headers: " + request.headers().map());
        System.out.println("Body: " + body);
        System.out.println("==============");
    }

    private void logResponse(HttpResponse<String> response) {
        System.out.println("=== RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("==============");
    }

    /**
     * Log del request en formato CURL
     * Ideal para copiar/pegar en Insomnia o terminal
     */
    private void logAsCurl(HttpRequest request, String body) {
        StringBuilder curl = new StringBuilder("curl -X ")
                .append(request.method())
                .append(" '")
                .append(request.uri())
                .append("'");

        request.headers().map().forEach((key, values) -> {
            for (String value : values) {
                curl.append(" \\\n  -H '")
                        .append(key)
                        .append(": ")
                        .append(value)
                        .append("'");
            }
        });

        if (body != null && !body.isBlank()) {
            curl.append(" \\\n  --data '")
                    .append(body.replace("'", "'\\''"))
                    .append("'");
        }

        System.out.println("=== CURL ===");
        System.out.println(curl);
        System.out.println("============");
    }

    // ================== Helpers ==================

    private boolean canRefresh(String url) {
        return SessionContext.isAuthenticated()
                && !url.contains("auth-login")
                && !url.contains("auth-refresh");
    }

    private Map<String, String> buildHeaders(Map<String, String> headers) {
        Map<String, String> finalHeaders = new HashMap<>();
        finalHeaders.put("Content-Type", "application/json; charset=UTF-8");
        finalHeaders.put("Accept", "application/json");

        if (headers != null) {
            finalHeaders.putAll(headers);
        }

        if (SessionContext.isAuthenticated()
                && !finalHeaders.containsKey("Authorization")) {
            finalHeaders.put(
                    "Authorization",
                    "Bearer " + SessionContext.getToken()
            );
        }

        return finalHeaders;
    }
}
