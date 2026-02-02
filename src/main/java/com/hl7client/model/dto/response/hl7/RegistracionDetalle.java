package com.hl7client.model.dto.response.hl7;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistracionDetalle {

    private Long transac;
    private Integer recha;
    private String denoItem;

    public Long getTransac() {
        return transac;
    }

    public Integer getRecha() {
        return recha;
    }

    public String getDenoItem() {
        return denoItem;
    }
}
