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
                toAbsolutePathString(result.licensePath()),
                toAbsolutePathString(result.publicKeyPath()),
                toAbsolutePathString(result.privateKeyPath()));
    }

    private static String toAbsolutePathString(java.nio.file.Path path) {
        return path != null ? path.toAbsolutePath().toString() : null;
    }
}
