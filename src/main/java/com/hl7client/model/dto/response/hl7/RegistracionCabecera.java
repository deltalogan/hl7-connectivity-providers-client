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

    public String getApeNom() {
        return apeNom;
    }

    @SuppressWarnings({"unused"})
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

    @SuppressWarnings({"unused"})
    public String getSexoAuto() {
        return sexoAuto;
    }

    @SuppressWarnings({"unused"})
    public String getGeneroAuto() {
        return generoAuto;
    }

    public Integer getEdad() {
        return edad;
    }

    @SuppressWarnings({"unused"})
    public String getLeyimp() {
        return leyimp;
    }

    @SuppressWarnings({"unused"})
    public String getIcdDeno() {
        return icdDeno;
    }

    @SuppressWarnings({"unused"})
    public String getNomPrestad() {
        return nomPrestad;
    }

    @SuppressWarnings({"unused"})
    public String getSucursal() {
        return sucursal;
    }

    @SuppressWarnings({"unused"})
    public Integer getAutoriz() {
        return autoriz;
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
    public void setSexoAuto(String sexoAuto) {
        this.sexoAuto = sexoAuto;
    }

    @SuppressWarnings({"unused"})
    public void setGeneroAuto(String generoAuto) {
        this.generoAuto = generoAuto;
    }

    @SuppressWarnings({"unused"})
    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    @SuppressWarnings({"unused"})
    public void setLeyimp(String leyimp) {
        this.leyimp = leyimp;
    }

    @SuppressWarnings({"unused"})
    public void setIcdDeno(String icdDeno) {
        this.icdDeno = icdDeno;
    }

    @SuppressWarnings({"unused"})
    public void setNomPrestad(String nomPrestad) {
        this.nomPrestad = nomPrestad;
    }

    @SuppressWarnings({"unused"})
    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    @SuppressWarnings({"unused"})
    public void setAutoriz(Integer autoriz) {
        this.autoriz = autoriz;
    }
}
