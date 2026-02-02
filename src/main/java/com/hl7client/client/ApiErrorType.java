package com.hl7client.client;

public enum ApiErrorType {
    CLIENT_ERROR,      // 4xx
    AUTH_ERROR,        // 401 / 403
    SERVER_ERROR,      // 5xx
    TECHNICAL_ERROR,   // timeout, IO, etc.
    UNKNOWN
}
