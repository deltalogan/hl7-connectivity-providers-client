package com.hl7client.model.dto.request.auth;

public class DeviceRequest {

    private final String messagingid;
    private final String deviceid;
    private final String devicename;
    private final boolean bloqueado;
    private final boolean recordar;

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
