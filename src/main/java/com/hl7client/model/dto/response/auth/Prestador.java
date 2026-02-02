package com.hl7client.model.dto.response.auth;

public class Prestador {

    private String emailPrestador;
    private String cuitPrestador;
    private String apellidoPrestador;
    private String razonSocialPrestador;
    private String habilitaADP;
    private Integer idPrestador;
    private Integer idUsuario;
    private Boolean admin;
    private Integer codPrestador;
    private String nombrePrestador;

    public Prestador() {
    }

    public String getEmailPrestador() {
        return emailPrestador;
    }

    public String getCuitPrestador() {
        return cuitPrestador;
    }

    public String getApellidoPrestador() {
        return apellidoPrestador;
    }

    public String getRazonSocialPrestador() {
        return razonSocialPrestador;
    }

    public String getHabilitaADP() {
        return habilitaADP;
    }

    public Integer getIdPrestador() {
        return idPrestador;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public Integer getCodPrestador() {
        return codPrestador;
    }

    public String getNombrePrestador() {
        return nombrePrestador;
    }
}
