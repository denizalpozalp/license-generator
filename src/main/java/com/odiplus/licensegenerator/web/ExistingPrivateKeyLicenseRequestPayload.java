package com.odiplus.licensegenerator.web;

import java.nio.file.Path;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odiplus.licensegenerator.licensing.ExistingPrivateKeyLicenseRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExistingPrivateKeyLicenseRequestPayload(
        @NotBlank String customerName,
        Instant issuedAt,
        @NotNull Instant expiresAt,
        String hardwareId,
        @NotBlank String licensePath,
        @NotBlank String privateKeyPath) {

    @JsonIgnore
    public ExistingPrivateKeyLicenseRequest toRequest() {
        return new ExistingPrivateKeyLicenseRequest(
                customerName,
                issuedAt,
                expiresAt,
                hardwareId,
                Path.of(licensePath),
                Path.of(privateKeyPath));
    }
}
