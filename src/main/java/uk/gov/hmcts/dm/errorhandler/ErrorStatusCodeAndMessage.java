package uk.gov.hmcts.dm.errorhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorStatusCodeAndMessage {

    private String message;
    private Integer statusCode;

}
