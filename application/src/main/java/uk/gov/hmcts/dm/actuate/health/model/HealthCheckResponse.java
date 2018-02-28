package uk.gov.hmcts.dm.actuate.health.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HealthCheckResponse {
    private String status;
}
