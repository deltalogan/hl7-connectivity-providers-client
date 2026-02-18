package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElegibilidadRequest {

    // --- Datos de operación ---
    private String modo;   // char(1)
    private String creden;           // char(19)
    private String alta;             // yyyy-MM-dd'T'HH:mm:ss
    private String fecdif;           // yyyy-MM-dd
    private Manual manual;        // char(1)

    // --- Datos técnicos ---
    private Integer ticketExt;
    private String termId;
    private Integer interNro;

    // --- Prestador ---
    private String cuit;
    private String oriMatri;

    // --- Resultado previo ---
    private Integer autoriz;
    private Integer rechaExt;

    // ---------- GETTERS ----------
    @SuppressWarnings({"unused"})
    public String getModo() {
        return modo;
    }

    @SuppressWarnings({"unused"})
    public String getCreden() {
        return creden;
    }

    @SuppressWarnings({"unused"})
    public String getAlta() {
        return alta;
    }

    @SuppressWarnings({"unused"})
    public String getFecdif() {
        return fecdif;
    }

    @SuppressWarnings({"unused"})
    public Manual getManual() {
        return manual;
    }

    @SuppressWarnings({"unused"})
    public Integer getTicketExt() {
        return ticketExt;
    }

    @SuppressWarnings({"unused"})
    public String getTermId() {
        return termId;
    }

    @SuppressWarnings({"unused"})
    public Integer getInterNro() {
        return interNro;
    }

    @SuppressWarnings({"unused"})
    public String getCuit() {
        return cuit;
    }

    @SuppressWarnings({"unused"})
    public String getOriMatri() {
        return oriMatri;
    }

    @SuppressWarnings({"unused"})
    public Integer getAutoriz() {
        return autoriz;
    }

    @SuppressWarnings({"unused"})
    public Integer getRechaExt() {
        return rechaExt;
    }

    // ---------- SETTERS ----------
    public void setModo(String modo) {
        this.modo = modo;
    }

    public void setCreden(String creden) {
        this.creden = creden;
    }

    public void setAlta(String alta) {
        this.alta = alta;
    }

    public void setFecdif(String fecdif) {
        this.fecdif = fecdif;
    }

    public void setManual(Manual manual) {
        this.manual = manual;
    }

    public void setTicketExt(Integer ticketExt) {
        this.ticketExt = ticketExt;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public void setInterNro(Integer interNro) {
        this.interNro = interNro;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public void setOriMatri(String oriMatri) {
        this.oriMatri = oriMatri;
    }

    public void setAutoriz(Integer autoriz) {
        this.autoriz = autoriz;
    }

    public void setRechaExt(Integer rechaExt) {
        this.rechaExt = rechaExt;
    }
}
