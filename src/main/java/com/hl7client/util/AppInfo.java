package com.hl7client.util;

public final class AppInfo {

    private static final String APP_NAME = "HL7 Connectivity";
    private static final String DEFAULT_VERSION = "1.0.0"; // fallback semántico

    private AppInfo() {
    }

    public static String getVersion() {
        String version = AppInfo.class
                .getPackage()
                .getImplementationVersion();

        if (version == null || version.isBlank()) {
            return DEFAULT_VERSION + " (dev)";
        }

        return version;
    }

    public static String loginTitle() {
        return String.format(
                "%s - Login (v%s)",
                APP_NAME,
                getVersion()
        );
    }

    public static String mainTitle(String prestador, String environment) {
        return String.format(
                "%s v%s | %s | %s",
                APP_NAME,
                getVersion(),
                prestador,
                environment
        );
    }
}
