package com.odiplus.licensegenerator.licensing;

public record LicenseVerificationResult(
        LicensePayload payload,
        String signature,
        boolean primaryKeyMatches,
        boolean secondaryKeyMatches) {
}
