package uk.gov.hmcts.dm.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CaseDocumentsDeletionResults {

    private Integer caseDocumentsFound;
    private Integer markedForDeletion;
}
