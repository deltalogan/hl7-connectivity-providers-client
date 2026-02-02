package com.hl7client.config;

import com.hl7client.util.PropertiesUtil;

public class EnvironmentConfig {

    private EnvironmentConfig() {
    }

    // ---------- BASE URL POR ENV ----------

    public static String getBaseUrl(Environment env) {
        return switch (env) {
            case DEV -> PropertiesUtil.get("env.base.url.dev");
            case QA -> PropertiesUtil.get("env.base.url.qa");
            case PRE -> PropertiesUtil.get("env.base.url.pre");
            case PRD -> PropertiesUtil.get("env.base.url.prd");
        };
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
