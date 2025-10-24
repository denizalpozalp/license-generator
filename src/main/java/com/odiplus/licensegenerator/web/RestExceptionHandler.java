package com.odiplus.licensegenerator.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.odiplus.licensegenerator.licensing.LicenseGenerationException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(LicenseGenerationException.class)
    public ProblemDetail handleLicenseGenerationException(LicenseGenerationException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setDetail(ex.getMessage());
        return detail;
    }
}
