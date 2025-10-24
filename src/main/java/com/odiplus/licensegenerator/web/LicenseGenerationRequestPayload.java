package com.odiplus.licensegenerator.web;

import java.nio.file.Path;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odiplus.licensegenerator.licensing.LicenseGenerationRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LicenseGenerationRequestPayload(
        @NotBlank String customerName,
        Instant issuedAt,
        @NotNull Instant expiresAt,
        String hardwareId,
        @NotBlank String licensePath,
        @NotBlank String publicKeyPath,
        @NotBlank String privateKeyPath,
        @Min(1024) Integer keySize) {

    private static final int DEFAULT_KEY_SIZE = 4096;

    @JsonIgnore
    public LicenseGenerationRequest toRequest() {
        int resolvedKeySize = keySize != null ? keySize : DEFAULT_KEY_SIZE;
        return new LicenseGenerationRequest(
                customerName,
                issuedAt,
                expiresAt,
                hardwareId,
                Path.of(licensePath),
                Path.of(publicKeyPath),
                Path.of(privateKeyPath),
                resolvedKeySize);
    }
}
