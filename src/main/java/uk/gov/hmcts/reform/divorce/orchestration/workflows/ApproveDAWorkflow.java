package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;


@Component
public class ApproveDAWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DocumentGenerationTask documentGenerationTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Autowired
    public ApproveDAWorkflow(final DocumentGenerationTask documentGenerationTask,
                                      final CaseFormatterAddDocuments caseFormatterAddDocuments) {
        this.documentGenerationTask = documentGenerationTask;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
    }

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {

        return this.execute(
            new Task[] { documentGenerationTask, caseFormatterAddDocuments},
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_FILENAME, DECREE_ABSOLUTE_FILENAME)
        );
    }

}
