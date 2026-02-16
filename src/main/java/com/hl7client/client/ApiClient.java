package com.hl7client.client;

import com.hl7client.config.SessionContext;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiClient {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    private final CloseableHttpClient httpClient;
    private final AuthRefresher authRefresher;

    public ApiClient(AuthRefresher authRefresher) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10_000)           // 10 segundos
                .setConnectionRequestTimeout(10_000)
                .setSocketTimeout(30_000)            // 30 segundos
                .setCookieSpec(CookieSpecs.STANDARD) // ← Soluciona warning de cookies Cloudflare (Expires con coma y año 4 dígitos)
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        this.authRefresher = Objects.requireNonNull(authRefresher);
    }

    public ApiResponse post(String url, String body, Map<String, String> headers) {
        return postInternal(url, body, headers, true);
    }

    private ApiResponse postInternal(String url, String body, Map<String, String> headers, boolean allowRetry) {
        HttpPost post = new HttpPost(url);

        // Headers
        Map<String, String> finalHeaders = buildHeaders(headers);
        finalHeaders.forEach(post::addHeader);

        // Body
        if (body != null && !body.trim().isEmpty()) {
            try {
                post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("Error preparando body", e);
            }
        }

        // Logging
        logRequest(post, body);
        logAsCurl(post, body);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            logResponse(statusCode, responseBody);

            // Refresh automático si 401 (solo una vez)
            if (statusCode == 401 && allowRetry && canRefresh(url)) {
                LOGGER.info("401 received, attempting auth refresh");
                authRefresher.refreshAuth();
                return postInternal(url, body, headers, false);
            }

            return new ApiResponse(statusCode, responseBody, Collections.emptyMap());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Transport error calling API", e);
            throw new RuntimeException("Error de comunicación con el servicio", e);
        }
    }

    // ================== Logging ==================

    private void logRequest(HttpPost post, String body) {
        System.out.println("=== REQUEST ===");
        System.out.println("URL: " + post.getURI());
        System.out.println("Method: POST");
        System.out.println("Headers: " + Arrays.toString(post.getAllHeaders()));
        System.out.println("Body: " + body);
        System.out.println("==============");
    }

    private void logResponse(int status, String body) {
        System.out.println("=== RESPONSE ===");
        System.out.println("Status: " + status);
        System.out.println("Body: " + body);
        System.out.println("==============");
    }

    private void logAsCurl(HttpPost post, String body) {
        StringBuilder curl = new StringBuilder("curl -X POST '").append(post.getURI()).append("'");

        for (org.apache.http.Header h : post.getAllHeaders()) {
            curl.append(" \\\n  -H '").append(h.getName()).append(": ").append(h.getValue()).append("'");
        }

        if (body != null && !body.trim().isEmpty()) {
            curl.append(" \\\n  --data '").append(body.replace("'", "'\\''")).append("'");
        }

        System.out.println("=== CURL ===");
        System.out.println(curl);
        System.out.println("============");
    }

    // Helpers
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

        if (SessionContext.isAuthenticated() && !finalHeaders.containsKey("Authorization")) {
            finalHeaders.put("Authorization", "Bearer " + SessionContext.getToken());
        }

        return finalHeaders;
    }

    // Buena práctica: cerrar el cliente cuando ya no se necesite
    public void close() throws IOException {
        httpClient.close();
    }
}