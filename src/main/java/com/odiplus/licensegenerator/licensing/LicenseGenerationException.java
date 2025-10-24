package com.odiplus.licensegenerator.licensing;

public class LicenseGenerationException extends RuntimeException {

    public LicenseGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicenseGenerationException(String message) {
        super(message);
    }
}
