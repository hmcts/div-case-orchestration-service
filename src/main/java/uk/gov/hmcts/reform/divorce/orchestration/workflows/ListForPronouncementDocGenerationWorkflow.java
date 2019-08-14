package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@Component
@RequiredArgsConstructor
public class ListForPronouncementDocGenerationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID = "FL-DIV-GNO-ENG-00059.docx";
    private static final String LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE = "caseListForPronouncement";
    private static final String LIST_FOR_PRONOUNCEMENT_FILE_NAME = "caseListForPronouncement";

    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    private final SyncBulkCaseListTask syncBulkCaseListTask;

    //TODO add task to trigger removeFromBulkCaseListed event on individual cases on cases with REMOVED_CASE_LIST on task context
    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {

        String judgeName = (String) ccdCallbackRequest.getCaseDetails().getCaseData().get(PRONOUNCEMENT_JUDGE_CCD_FIELD);
        List<Task> taskList = new ArrayList<>();
        taskList.add(syncBulkCaseListTask);
        if (StringUtils.isNotBlank(judgeName)) {
            taskList.add(setFormattedDnCourtDetails);
            taskList.add(documentGenerationTask);
            taskList.add(caseFormatterAddDocuments);
        }

        Task[] taskArr = new Task[taskList.size()];
        return this.execute(
            taskList.toArray(taskArr),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_FILENAME, LIST_FOR_PRONOUNCEMENT_FILE_NAME)
        );
    }
}
