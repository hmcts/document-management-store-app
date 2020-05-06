package uk.gov.hmcts.dm.service.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class DeleteAllExpiredStoredDocumentTasklet implements Tasklet {

    @Autowired
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        auditedStoredDocumentBatchOperationsService.hardDeleteAllExpiredStoredDocuments();
        return RepeatStatus.FINISHED;
    }
}
