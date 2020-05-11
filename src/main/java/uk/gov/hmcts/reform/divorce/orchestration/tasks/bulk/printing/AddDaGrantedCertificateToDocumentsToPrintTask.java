package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.getExistingDaGrantedFromCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint;

@Component
@AllArgsConstructor
public class AddDaGrantedCertificateToDocumentsToPrintTask implements Task<Map<String, Object>> {

    @NoArgsConstructor
    public static class FileMetadata {
        public static final String DOCUMENT_TYPE = OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        appendAnotherDocumentToBulkPrint(
            context,
            getExistingDaGrantedFromCaseData(caseData)
        );

        return caseData;
    }
}
