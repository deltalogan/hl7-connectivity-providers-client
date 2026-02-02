package com.hl7client.client;

/**
 * Responsible for refreshing authentication credentials.
 * <p>
 * Implementations must:
 * - Perform a synchronous refresh operation
 * - Update {@link com.hl7client.config.SessionContext} on success
 * - Throw an exception on failure
 * <p>
 * Any thrown exception will be treated as a terminal session failure.
 */
public interface AuthRefresher {

    /**
     * Refreshes authentication credentials.
     *
     * @throws RuntimeException if the refresh fails
     */
    void refreshAuth();
}
