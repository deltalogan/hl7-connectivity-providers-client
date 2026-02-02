package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElegibilidadResponse {

    private String planCodi;
    private String sexoAuto;
    private String generoAuto;
    private String apeNom;
    private String transacAlta;
    private String pmi;
    private Integer edad;
    private String transac;
    private Integer rechaCabecera;
    private String icdDeno;
    private String gravado;
    private String rechaCabeDeno;
    private String sexo;
    private String leyimp;

    // ---------- GETTERS ----------

    public String getPlanCodi() {
        return planCodi;
    }

    public String getSexoAuto() {
        return sexoAuto;
    }

    public String getGeneroAuto() {
        return generoAuto;
    }

    public String getApeNom() {
        return apeNom;
    }

    public String getTransacAlta() {
        return transacAlta;
    }

    public String getPmi() {
        return pmi;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getTransac() {
        return transac;
    }

    public Integer getRechaCabecera() {
        return rechaCabecera;
    }

    public String getIcdDeno() {
        return icdDeno;
    }

    public String getGravado() {
        return gravado;
    }

    public String getRechaCabeDeno() {
        return rechaCabeDeno;
    }

    public String getSexo() {
        return sexo;
    }

    public String getLeyimp() {
        return leyimp;
    }
}
