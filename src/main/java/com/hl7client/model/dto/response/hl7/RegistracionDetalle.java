package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistracionDetalle {

    private Long transac;
    private Integer recha;
    private String denoItem;

    @SuppressWarnings({"unused"})
    public Long getTransac() {
        return transac;
    }

    public Integer getRecha() {
        return recha;
    }

    public String getDenoItem() {
        return denoItem;
    }

    @SuppressWarnings({"unused"})
    public void setTransac(Long transac) {
        this.transac = transac;
    }

    @SuppressWarnings({"unused"})
    public void setRecha(Integer recha) {
        this.recha = recha;
    }

    @SuppressWarnings({"unused"})
    public void setDenoItem(String denoItem) {
        this.denoItem = denoItem;
    }
}
