package com.hl7client.model.dto.request.hl7;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistracionRequest {

    // ===============================
    // Datos de operación
    // ===============================

    private ModoRegistracion modo;   // @modo char(1)
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

    public ModoRegistracion getModo() {
        return modo;
    }

    public String getCreden() {
        return creden;
    }

    public Integer getTipo() {
        return tipo;
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

    public Manual getConsulta() {
        return consulta;
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

    public String getIcd() {
        return icd;
    }

    public String getParam1() {
        return param1;
    }

    public String getParam2() {
        return param2;
    }

    public String getParam3() {
        return param3;
    }

    public String getTipoEfector() {
        return tipoEfector;
    }

    public String getIdEfector() {
        return idEfector;
    }

    public String getTipoPrescr() {
        return tipoPrescr;
    }

    public String getIdPrescr() {
        return idPrescr;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getAckacept() {
        return ackacept;
    }

    public String getAckackapl() {
        return ackackapl;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public Boolean getPowerBuilder() {
        return powerBuilder;
    }

    // ===============================
    // SETTERS
    // ===============================

    public void setModo(ModoRegistracion modo) {
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
