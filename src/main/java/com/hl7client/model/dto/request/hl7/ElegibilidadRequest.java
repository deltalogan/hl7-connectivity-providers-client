package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElegibilidadRequest {

    // --- Datos de operación ---
    private ModoElegibilidad modo;   // char(1)
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
    public ModoElegibilidad getModo() {
        return modo;
    }

    public String getCreden() {
        return creden;
    }

    public String getAlta() {
        return alta;
    }

    public String getFecdif() {
        return fecdif;
    }

    public Manual getManual() {
        return manual;
    }

    public Integer getTicketExt() {
        return ticketExt;
    }

    public String getTermId() {
        return termId;
    }

    public Integer getInterNro() {
        return interNro;
    }

    public String getCuit() {
        return cuit;
    }

    public String getOriMatri() {
        return oriMatri;
    }

    public Integer getAutoriz() {
        return autoriz;
    }

    public Integer getRechaExt() {
        return rechaExt;
    }

    // ---------- SETTERS ----------
    public void setModo(ModoElegibilidad modo) {
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
