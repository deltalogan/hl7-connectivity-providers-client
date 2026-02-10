package com.hl7client.hl7;

public class Hl7BenefitSerializationResult {

    private final String param1;
    private final String param2;
    private final String param3;

    public Hl7BenefitSerializationResult(
            String param1,
            String param2,
            String param3
    ) {
        this.param1 = param1 != null ? param1 : "";
        this.param2 = param2 != null ? param2 : "";
        this.param3 = param3 != null ? param3 : "";
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
}
