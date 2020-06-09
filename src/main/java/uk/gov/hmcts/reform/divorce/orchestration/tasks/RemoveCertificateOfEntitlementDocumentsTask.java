package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;

@Component
@RequiredArgsConstructor
public class RemoveCertificateOfEntitlementDocumentsTask implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        return caseFormatterClient.removeAllDocumentsByType(DOCUMENT_TYPE_COE, caseData);
    }
}
