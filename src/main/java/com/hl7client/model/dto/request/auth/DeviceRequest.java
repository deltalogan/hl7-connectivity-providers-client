package com.hl7client.model.dto.request.auth;

public class DeviceRequest {

    private String messagingid;
    private String deviceid;
    private String devicename;
    private boolean bloqueado;
    private boolean recordar;

    public DeviceRequest(String messagingid, String deviceid, String devicename) {
        this.messagingid = messagingid;
        this.deviceid = deviceid;
        this.devicename = devicename;
        this.bloqueado = false;
        this.recordar = false;
    }

    @SuppressWarnings("unused")
    public String getMessagingid() {
        return messagingid;
    }

    @SuppressWarnings("unused")
    public String getDeviceid() {
        return deviceid;
    }

    @SuppressWarnings("unused")
    public String getDevicename() {
        return devicename;
    }

    @SuppressWarnings("unused")
    public boolean isBloqueado() {
        return bloqueado;
    }

    @SuppressWarnings("unused")
    public boolean isRecordar() {
        return recordar;
    }
}
