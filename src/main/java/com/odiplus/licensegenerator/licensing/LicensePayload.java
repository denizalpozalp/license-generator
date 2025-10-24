package com.odiplus.licensegenerator.licensing;

import java.time.Instant;

public record LicensePayload(
        String customerName,
        Instant issuedAt,
        Instant expiresAt,
        String hardwareId) {
}
