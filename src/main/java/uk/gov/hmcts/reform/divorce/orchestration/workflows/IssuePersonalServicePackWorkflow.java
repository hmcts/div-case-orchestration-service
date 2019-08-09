package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendSolicitorPersonalServiceEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID;

@Component
@RequiredArgsConstructor
public class IssuePersonalServicePackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DocumentGenerationTask documentGenerationTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    private final SendSolicitorPersonalServiceEmailTask sendSolicitorPersonalServiceEmailTask;

    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                documentGenerationTask,
                caseFormatterAddDocuments,
                sendSolicitorPersonalServiceEmailTask
            },
            callbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, callbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, callbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_FILENAME, SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME)
        );
    }
}
