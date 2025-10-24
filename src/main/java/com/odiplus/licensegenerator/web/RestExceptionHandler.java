package com.odiplus.licensegenerator.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.odiplus.licensegenerator.licensing.LicenseGenerationException;
import com.odiplus.licensegenerator.licensing.LicenseVerificationException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({ LicenseGenerationException.class, LicenseVerificationException.class })
    public ProblemDetail handleLicenseExceptions(RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setDetail(ex.getMessage());
        return detail;
    }
}
