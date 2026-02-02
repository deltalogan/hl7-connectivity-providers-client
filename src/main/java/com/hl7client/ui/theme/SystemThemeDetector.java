package com.hl7client.ui.theme;

import java.util.Optional;

public final class SystemThemeDetector {

    private SystemThemeDetector() {
    }

    /**
     * @return Optional.of(true)  → dark
     * Optional.of(false) → light
     * Optional.empty()   → no detectable
     */
    public static Optional<Boolean> isDark() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                return detectWindows();
            }
            if (os.contains("mac")) {
                return detectMac();
            }
            if (os.contains("linux")) {
                return detectLinux();
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }

    // ================= SO =================

    private static Optional<Boolean> detectWindows() {
        try {
            Process process = new ProcessBuilder(
                    "reg",
                    "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v",
                    "AppsUseLightTheme"
            ).start();

            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {

                return reader.lines()
                        .filter(line -> line.contains("AppsUseLightTheme"))
                        .map(line -> line.trim().endsWith("0"))
                        .findFirst();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> detectMac() {
        try {
            Process process = new ProcessBuilder(
                    "defaults",
                    "read",
                    "-g",
                    "AppleInterfaceStyle"
            ).start();

            return Optional.of(process.waitFor() == 0);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> detectLinux() {
        // Linux es muy variable → GNOME/KDE/etc
        return Optional.empty();
    }
}

