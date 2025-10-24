package com.odiplus.licensegenerator.web;

import com.odiplus.licensegenerator.licensing.LicenseGenerationResult;
import com.odiplus.licensegenerator.licensing.LicensePayload;

public record LicenseGenerationResponse(
        LicensePayload payload,
        String signature,
        String licensePath,
        String publicKeyPath,
        String privateKeyPath) {

    public static LicenseGenerationResponse fromResult(LicenseGenerationResult result) {
        return new LicenseGenerationResponse(
                result.payload(),
                result.signature(),
                result.licensePath().toAbsolutePath().toString(),
                result.publicKeyPath().toAbsolutePath().toString(),
                result.privateKeyPath().toAbsolutePath().toString());
    }
}
