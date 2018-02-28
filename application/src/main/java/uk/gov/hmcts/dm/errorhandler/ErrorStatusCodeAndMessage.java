package uk.gov.hmcts.dm.errorhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pawel on 23/10/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorStatusCodeAndMessage {

    private String message;
    private Integer statusCode;

}
