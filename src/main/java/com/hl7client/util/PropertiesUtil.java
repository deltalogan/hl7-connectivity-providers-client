package com.hl7client.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is =
                     PropertiesUtil.class
                             .getClassLoader()
                             .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("No se encontró application.properties");
            }

            properties.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Error cargando application.properties", e);
        }
    }

    private PropertiesUtil() {
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Property no encontrada: " + key
            );
        }
        return value;
    }
}
