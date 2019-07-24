package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
public class IssueAosPackOfflineWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private DocumentGenerationTask documentGenerationTask;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails) throws WorkflowException {
        return execute(
            new Task[] {
                documentGenerationTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, AOS_INVITATION_LETTER_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_TYPE, AOS_INVITATION_LETTER_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_FILENAME, AOS_INVITATION_LETTER_FILENAME)
        );

    }

}