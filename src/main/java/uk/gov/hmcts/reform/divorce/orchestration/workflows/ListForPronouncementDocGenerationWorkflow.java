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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SyncBulkCaseListTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
@RequiredArgsConstructor
public class ListForPronouncementDocGenerationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String templateId = "FL-DIV-GNO-ENG-00059.docx";
    public static final String  documentType = "caseListForPronouncement";
    public static final String  filename = "caseListForPronouncement";
    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    private final SyncBulkCaseListTask syncBulkCaseListTask;


    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {

        return this.execute(
            new Task[] {syncBulkCaseListTask, setFormattedDnCourtDetails, documentGenerationTask, caseFormatterAddDocuments},
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, documentType),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, templateId),
            ImmutablePair.of(DOCUMENT_FILENAME, filename)
        );
    }

}
