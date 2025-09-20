package uk.gov.hmcts.dm.service;

import java.util.Optional;

public record FileVerificationResult(boolean valid, String detectedMimeType) {

    public Optional<String> getDetectedMimeType() {
        return Optional.ofNullable(detectedMimeType);
    }

    public static FileVerificationResult invalid() {
        return new FileVerificationResult(false, null);
    }
}
