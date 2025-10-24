package com.odiplus.licensegenerator.licensing;

public class LicenseVerificationException extends RuntimeException {

    public LicenseVerificationException(String message) {
        super(message);
    }

    public LicenseVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
