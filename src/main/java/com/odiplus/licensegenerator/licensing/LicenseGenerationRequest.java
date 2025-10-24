package com.odiplus.licensegenerator.licensing;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public record LicenseGenerationRequest(
        String customerName,
        Instant issuedAt,
        Instant expiresAt,
        String hardwareId,
        Path licensePath,
        Path publicKeyPath,
        Path privateKeyPath,
        int keySize) {

    public LicenseGenerationRequest {
        Objects.requireNonNull(customerName, "customerName must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(licensePath, "licensePath must not be null");
        Objects.requireNonNull(publicKeyPath, "publicKeyPath must not be null");
        Objects.requireNonNull(privateKeyPath, "privateKeyPath must not be null");
        if (keySize < 1024) {
            throw new IllegalArgumentException("keySize must be at least 1024 bits");
        }
    }
}
