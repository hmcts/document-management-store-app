package uk.gov.hmcts.dm.service;

import lombok.Getter;

import java.util.Optional;


public final class FileVerificationResult {

    @Getter
    private final boolean valid;
    private final String detectedMimeType;

    public FileVerificationResult(boolean valid, String detectedMimeType) {
        this.valid = valid;
        this.detectedMimeType = detectedMimeType;
    }

    public Optional<String> getDetectedMimeType() {
        return Optional.ofNullable(detectedMimeType);
    }

    public static FileVerificationResult invalid() {
        return new FileVerificationResult(false, null);
    }
}
