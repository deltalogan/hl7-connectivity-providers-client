package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelacionRequest {

    // ---------- Operación ----------
    private String modo;     // "N"
    private Long creden;              // 7180171001151
    private Integer tipo;             // 2
    private String alta;              // puede venir vacío
    private Manual manual;         // nullable

    // ---------- Cancelación ----------
    private Integer ticketExt;        // 0
    private Integer cancelCab;        // nullable
    private String cancelModo; // "N"
    private Integer errorExt;          // nullable

    // ---------- Técnica ----------
    private String termId;
    private Integer interNro;

    // ---------- Prestador ----------
    private Long cuit;

    // ---------- Parámetros HL7 libres ----------
    private String param1;
    private String param2;

    // ---------- GETTERS ----------

    public String getModo() {
        return modo;
    }

    public Long getCreden() {
        return creden;
    }

    public Integer getTipo() {
        return tipo;
    }

    public String getAlta() {
        return alta;
    }

    public Manual getManual() {
        return manual;
    }

    public Integer getTicketExt() {
        return ticketExt;
    }

    public Integer getCancelCab() {
        return cancelCab;
    }

    public String getCancelModo() {
        return cancelModo;
    }

    public Integer getErrorExt() {
        return errorExt;
    }

    public String getTermId() {
        return termId;
    }

    public Integer getInterNro() {
        return interNro;
    }

    public Long getCuit() {
        return cuit;
    }

    public String getParam1() {
        return param1;
    }

    public String getParam2() {
        return param2;
    }

    // ---------- SETTERS ----------

    public void setModo(String modo) {
        this.modo = modo;
    }

    public void setCreden(Long creden) {
        this.creden = creden;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public void setAlta(String alta) {
        this.alta = alta;
    }

    public void setManual(Manual manual) {
        this.manual = manual;
    }

    public void setTicketExt(Integer ticketExt) {
        this.ticketExt = ticketExt;
    }

    public void setCancelCab(Integer cancelCab) {
        this.cancelCab = cancelCab;
    }

    public void setErrorExt(Integer errorExt) {
        this.errorExt = errorExt;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public void setInterNro(Integer interNro) {
        this.interNro = interNro;
    }

    public void setCuit(Long cuit) {
        this.cuit = cuit;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }
}
