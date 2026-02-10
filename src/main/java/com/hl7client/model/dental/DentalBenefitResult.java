package com.hl7client.model.dental;

public class DentalBenefitResult {

    private final String benefitCode;
    private final String param1;

    public DentalBenefitResult(String benefitCode, String param1) {
        this.benefitCode = benefitCode;
        this.param1 = param1;
    }

    public String getBenefitCode() {
        return benefitCode;
    }

    public String getParam1() {
        return param1;
    }
}
