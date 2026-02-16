package com.hl7client.config;

import com.hl7client.util.PropertiesUtil;

public class EnvironmentConfig {

    private EnvironmentConfig() {
    }

    // ---------- BASE URL POR ENV ----------

    public static String getBaseUrl(Environment env) {
        String urlKey;
        switch (env) {
            case DEV:
                urlKey = "env.base.url.dev";
                break;
            case QA:
                urlKey = "env.base.url.qa";
                break;
            case PRE:
                urlKey = "env.base.url.pre";
                break;
            case PRD:
                urlKey = "env.base.url.prd";
                break;
            default:
                throw new IllegalArgumentException("Entorno no soportado: " + env);
        }
        return PropertiesUtil.get(urlKey);
    }

    // ---------- AUTH ----------

    public static String getAuthUrl(Environment env) {
        return getBaseUrl(env)
                + PropertiesUtil.get("api.context.path")
                + "/v0/auth-login";
    }

    public static String getAuthRefreshUrl(Environment env) {
        return getBaseUrl(env)
                + PropertiesUtil.get("api.context.path")
                + "/v0/auth-refresh";
    }

    // ---------- HL7 ----------

    public static String getHl7ElegibilidadUrl(Environment env) {
        return getBaseUrl(env)
                + PropertiesUtil.get("api.context.path")
                + PropertiesUtil.get("api.version.v3")
                + PropertiesUtil.get("hl7.context.path")
                + "/elegibilidad";
    }

    public static String getHl7RegistracionUrl(Environment env) {
        return getBaseUrl(env)
                + PropertiesUtil.get("api.context.path")
                + PropertiesUtil.get("api.version.v3")
                + PropertiesUtil.get("hl7.context.path")
                + "/registracion";
    }

    public static String getHl7CancelacionUrl(Environment env) {
        return getBaseUrl(env)
                + PropertiesUtil.get("api.context.path")
                + PropertiesUtil.get("api.version.v2")
                + "/prestadores/hl7/cancela-prestacion";
    }

}
