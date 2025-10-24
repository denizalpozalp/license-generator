package com.odiplus.licensegenerator.web;

import com.odiplus.licensegenerator.licensing.LicensePayload;
import com.odiplus.licensegenerator.licensing.LicenseVerificationResult;

public record LicenseVerificationResponse(
        LicensePayload payload,
        String signature,
        boolean primaryKeyMatches,
        boolean secondaryKeyMatches) {

    public static LicenseVerificationResponse fromResult(LicenseVerificationResult result) {
        return new LicenseVerificationResponse(
                result.payload(),
                result.signature(),
                result.primaryKeyMatches(),
                result.secondaryKeyMatches());
    }
}
