package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PersonalServiceValidationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME;


@Component
@RequiredArgsConstructor
public class IssuePersonalServicePackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PersonalServiceValidationTask personalServiceValidationTask;
    private final DocumentGenerationTask documentGenerationTask;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final DocumentTemplateService documentTemplateService;

    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getCaseData();
        String templateId = getTemplateId(documentTemplateService,
                DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID,
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        return this.execute(
            new Task[]{
                personalServiceValidationTask,
                documentGenerationTask,
                caseFormatterAddDocuments
            },
            callbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, callbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, callbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, templateId),
            ImmutablePair.of(DOCUMENT_FILENAME, SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME)
        );
    }
}
