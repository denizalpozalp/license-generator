package com.odiplus.licensegenerator.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.odiplus.licensegenerator.licensing.LicenseGenerationResult;
import com.odiplus.licensegenerator.licensing.LicenseGenerator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/licenses")
@Validated
public class LicenseController {

    private final LicenseGenerator licenseGenerator;

    public LicenseController(LicenseGenerator licenseGenerator) {
        this.licenseGenerator = licenseGenerator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LicenseGenerationResponse generate(@RequestBody @Valid LicenseGenerationRequestPayload payload) {
        LicenseGenerationResult result = licenseGenerator.generate(payload.toRequest());
        return LicenseGenerationResponse.fromResult(result);
    }
}
