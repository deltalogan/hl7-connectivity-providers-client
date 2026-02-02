package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelacionCabecera {

    private String transacAlta;
    private Long transac;
    private Integer rechaCabecera;
    private String rechaCabeDeno;

    private String apeNom;
    private String gravado;
    private String planCodi;
    private String pmi;
    private String sexo;
    private Integer edad;
    private String leyimp;

    // getters

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

    public Integer getEdad() {
        return edad;
    }

    public String getLeyimp() {
        return leyimp;
    }
}
