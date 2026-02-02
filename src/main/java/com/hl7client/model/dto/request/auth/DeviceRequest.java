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

    public String getMessagingid() {
        return messagingid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public String getDevicename() {
        return devicename;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public boolean isRecordar() {
        return recordar;
    }
}
