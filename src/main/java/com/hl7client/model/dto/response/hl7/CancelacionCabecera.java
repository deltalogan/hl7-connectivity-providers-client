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

    @SuppressWarnings({"unused"})
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

    @SuppressWarnings({"unused"})
    public String getApeNom() {
        return apeNom;
    }

    @SuppressWarnings({"unused"})
    public String getGravado() {
        return gravado;
    }

    @SuppressWarnings({"unused"})
    public String getPlanCodi() {
        return planCodi;
    }

    @SuppressWarnings({"unused"})
    public String getPmi() {
        return pmi;
    }

    @SuppressWarnings({"unused"})
    public String getSexo() {
        return sexo;
    }

    @SuppressWarnings({"unused"})
    public Integer getEdad() {
        return edad;
    }

    @SuppressWarnings({"unused"})
    public String getLeyimp() {
        return leyimp;
    }

    @SuppressWarnings({"unused"})
    public void setTransacAlta(String transacAlta) {
        this.transacAlta = transacAlta;
    }

    @SuppressWarnings({"unused"})
    public void setTransac(Long transac) {
        this.transac = transac;
    }

    @SuppressWarnings({"unused"})
    public void setRechaCabecera(Integer rechaCabecera) {
        this.rechaCabecera = rechaCabecera;
    }

    @SuppressWarnings({"unused"})
    public void setRechaCabeDeno(String rechaCabeDeno) {
        this.rechaCabeDeno = rechaCabeDeno;
    }

    @SuppressWarnings({"unused"})
    public void setApeNom(String apeNom) {
        this.apeNom = apeNom;
    }

    @SuppressWarnings({"unused"})
    public void setGravado(String gravado) {
        this.gravado = gravado;
    }

    @SuppressWarnings({"unused"})
    public void setPlanCodi(String planCodi) {
        this.planCodi = planCodi;
    }

    @SuppressWarnings({"unused"})
    public void setPmi(String pmi) {
        this.pmi = pmi;
    }

    @SuppressWarnings({"unused"})
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    @SuppressWarnings({"unused"})
    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    @SuppressWarnings({"unused"})
    public void setLeyimp(String leyimp) {
        this.leyimp = leyimp;
    }
}
