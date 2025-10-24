package com.odiplus.licensegenerator.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.odiplus.licensegenerator.licensing.LicenseGenerationResult;
import com.odiplus.licensegenerator.licensing.LicenseGenerator;
import com.odiplus.licensegenerator.licensing.LicenseVerificationException;
import com.odiplus.licensegenerator.licensing.LicenseVerificationResult;
import com.odiplus.licensegenerator.licensing.LicenseVerifier;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/licenses")
@Validated
public class LicenseController {

    private final LicenseGenerator licenseGenerator;
    private final LicenseVerifier licenseVerifier;

    public LicenseController(LicenseGenerator licenseGenerator, LicenseVerifier licenseVerifier) {
        this.licenseGenerator = licenseGenerator;
        this.licenseVerifier = licenseVerifier;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LicenseGenerationResponse generate(@RequestBody @Valid LicenseGenerationRequestPayload payload) {
        LicenseGenerationResult result = licenseGenerator.generate(payload.toRequest());
        return LicenseGenerationResponse.fromResult(result);
    }

    @PostMapping(path = "/sign")
    @ResponseStatus(HttpStatus.CREATED)
    public LicenseGenerationResponse generateWithExistingPrivateKey(
            @RequestBody @Valid ExistingPrivateKeyLicenseRequestPayload payload) {
        LicenseGenerationResult result = licenseGenerator.generateWithExistingPrivateKey(payload.toRequest());
        return LicenseGenerationResponse.fromResult(result);
    }

    @PostMapping(path = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LicenseVerificationResponse verify(
            @RequestPart("license") MultipartFile licenseFile,
            @RequestPart("primaryKey") MultipartFile primaryKeyFile,
            @RequestPart("secondaryKey") MultipartFile secondaryKeyFile) {
        try {
            String licenseContent = new String(licenseFile.getBytes(), StandardCharsets.UTF_8);
            String primaryKeyContent = new String(primaryKeyFile.getBytes(), StandardCharsets.UTF_8);
            String secondaryKeyContent = new String(secondaryKeyFile.getBytes(), StandardCharsets.UTF_8);

            LicenseVerificationResult result = licenseVerifier.verify(licenseContent, primaryKeyContent, secondaryKeyContent);
            return LicenseVerificationResponse.fromResult(result);
        } catch (IOException ex) {
            throw new LicenseVerificationException("Yüklenen dosyalar okunamadı.", ex);
        }
    }
}
