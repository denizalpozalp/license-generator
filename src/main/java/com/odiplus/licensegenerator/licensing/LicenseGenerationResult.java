package com.odiplus.licensegenerator.licensing;

import java.nio.file.Path;

public record LicenseGenerationResult(
        Path licensePath,
        Path publicKeyPath,
        Path privateKeyPath,
        LicensePayload payload,
        String signature) {
}
