package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistracionCabecera {

    private String transacAlta;
    private Long transac;

    private Integer rechaCabecera;
    private String rechaCabeDeno;

    private String apeNom;
    private String gravado;
    private String planCodi;
    private String pmi;

    private String sexo;
    private String sexoAuto;
    private String generoAuto;

    private Integer edad;
    private String leyimp;

    private String icdDeno;
    private String nomPrestad;
    private String sucursal;

    private Integer autoriz;

    // ---------- GETTERS ----------

    public String getTransacAlta() {
        return transacAlta;
    }

    public Long getTransac() {
        return transac;
    }

    public Integer getRechaCabecera() {
        return rechaCabecera;
    }

    public String getRechaCabeDeno() {
        return rechaCabeDeno;
    }

    public String getApeNom() {
        return apeNom;
    }

    public String getGravado() {
        return gravado;
    }

    public String getPlanCodi() {
        return planCodi;
    }

    public String getPmi() {
        return pmi;
    }

    public String getSexo() {
        return sexo;
    }

    public String getSexoAuto() {
        return sexoAuto;
    }

    public String getGeneroAuto() {
        return generoAuto;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getLeyimp() {
        return leyimp;
    }

    public String getIcdDeno() {
        return icdDeno;
    }

    public String getNomPrestad() {
        return nomPrestad;
    }

    public String getSucursal() {
        return sucursal;
    }

    public Integer getAutoriz() {
        return autoriz;
    }
}
