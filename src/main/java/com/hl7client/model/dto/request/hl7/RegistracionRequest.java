package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistracionRequest {

    // ===============================
    // Datos de operación
    // ===============================

    private String modo;   // @modo char(1)
    private String creden;           // @creden char(23)
    private Integer tipo;            // @tipo tinyint
    private String alta;             // @alta datetime
    private String fecdif;           // @fecdif varchar(10)
    private Manual manual;        // @manual char(1)
    private Manual consulta;      // @consulta char(1)

    // ===============================
    // Datos técnicos
    // ===============================

    private Integer ticketExt;        // @ticket_ext int
    private String termId;            // @term_id char(20)
    private Integer interNro;          // @inter_nro int

    // ===============================
    // Prestador
    // ===============================

    private String cuit;              // @cuit char(40)
    private String oriMatri;           // @ori_matri char(10)

    // ===============================
    // Resultado previo
    // ===============================

    private Integer autoriz;           // @autoriz int
    private Integer rechaExt;           // @recha_ext smallint

    // ===============================
    // HL7 extendido
    // ===============================

    private String icd;                // @icd char(6)

    private String param1;             // @param1 varchar(255)
    private String param2;             // @param2 varchar(255)
    private String param3;             // @param3 varchar(255)

    private String tipoEfector;        // @tipoefector char(4)
    private String idEfector;          // @idefector char(11)

    private String tipoPrescr;         // @tipoprescr char(4)
    private String idPrescr;           // @idprescr char(11)

    private String msgId;              // @msgid char(20)
    private String ackacept;           // @ackacept char(2)
    private String ackackapl;          // @ackackapl char(2)

    private String tipoMensaje;        // @tipo_mensaje char(1)
    private Boolean powerBuilder;      // @power_builder bit

    // ===============================
    // GETTERS
    // ===============================

    @SuppressWarnings({"unused"})
    public String getModo() {
        return modo;
    }

    @SuppressWarnings({"unused"})
    public String getCreden() {
        return creden;
    }

    @SuppressWarnings({"unused"})
    public Integer getTipo() {
        return tipo;
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
    public Manual getConsulta() {
        return consulta;
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

    @SuppressWarnings({"unused"})
    public String getIcd() {
        return icd;
    }

    public String getParam1() {
        return param1;
    }

    public String getParam2() {
        return param2;
    }

    @SuppressWarnings({"unused"})
    public String getParam3() {
        return param3;
    }

    @SuppressWarnings({"unused"})
    public String getTipoEfector() {
        return tipoEfector;
    }

    @SuppressWarnings({"unused"})
    public String getIdEfector() {
        return idEfector;
    }

    @SuppressWarnings({"unused"})
    public String getTipoPrescr() {
        return tipoPrescr;
    }

    @SuppressWarnings({"unused"})
    public String getIdPrescr() {
        return idPrescr;
    }

    @SuppressWarnings({"unused"})
    public String getMsgId() {
        return msgId;
    }

    @SuppressWarnings({"unused"})
    public String getAckacept() {
        return ackacept;
    }

    @SuppressWarnings({"unused"})
    public String getAckackapl() {
        return ackackapl;
    }

    @SuppressWarnings({"unused"})
    public String getTipoMensaje() {
        return tipoMensaje;
    }

    @SuppressWarnings({"unused"})
    public Boolean getPowerBuilder() {
        return powerBuilder;
    }

    // ===============================
    // SETTERS
    // ===============================

    public void setModo(String modo) {
        this.modo = modo;
    }

    public void setCreden(String creden) {
        this.creden = creden;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
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

    public void setConsulta(Manual consulta) {
        this.consulta = consulta;
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

    public void setIcd(String icd) {
        this.icd = icd;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public void setTipoEfector(String tipoEfector) {
        this.tipoEfector = tipoEfector;
    }

    public void setIdEfector(String idEfector) {
        this.idEfector = idEfector;
    }

    public void setTipoPrescr(String tipoPrescr) {
        this.tipoPrescr = tipoPrescr;
    }

    public void setIdPrescr(String idPrescr) {
        this.idPrescr = idPrescr;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setAckacept(String ackacept) {
        this.ackacept = ackacept;
    }

    public void setAckackapl(String ackackapl) {
        this.ackackapl = ackackapl;
    }

    public void setTipoMensaje(String tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }

    public void setPowerBuilder(Boolean powerBuilder) {
        this.powerBuilder = powerBuilder;
    }
}
