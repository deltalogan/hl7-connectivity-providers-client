package com.hl7client.client;

import java.util.Map;

public final class ApiResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public ApiResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isHttpError() {
        return statusCode >= 400;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", body='" + body + '\'' +
                '}';
    }
}
