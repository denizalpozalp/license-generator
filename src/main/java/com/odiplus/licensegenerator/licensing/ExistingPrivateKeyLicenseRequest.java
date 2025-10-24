package com.odiplus.licensegenerator.licensing;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public record ExistingPrivateKeyLicenseRequest(
        String customerName,
        Instant issuedAt,
        Instant expiresAt,
        String hardwareId,
        Path licensePath,
        Path privateKeyPath) {

    public ExistingPrivateKeyLicenseRequest {
        Objects.requireNonNull(customerName, "customerName must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(licensePath, "licensePath must not be null");
        Objects.requireNonNull(privateKeyPath, "privateKeyPath must not be null");
    }
}
