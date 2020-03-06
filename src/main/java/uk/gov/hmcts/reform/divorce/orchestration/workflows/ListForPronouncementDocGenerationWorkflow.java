package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SyncBulkCaseListTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseRemovePronouncementDetailsWithinBulkTask;

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

    private static final String LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE = "caseListForPronouncement";
    private static final String LIST_FOR_PRONOUNCEMENT_FILE_NAME = "caseListForPronouncement";

    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    private final SyncBulkCaseListTask syncBulkCaseListTask;

    private final UpdateDivorceCaseRemovePronouncementDetailsWithinBulkTask removePronouncementDetailsTask;
    private final DocumentTemplateService documentTemplateService;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getCaseData();

        String judgeName = (String) caseData.get(PRONOUNCEMENT_JUDGE_CCD_FIELD);
        final String templateId = getTemplateId(documentTemplateService,
                DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID,
                caseData);

        List<Task> taskList = new ArrayList<>();
        taskList.add(syncBulkCaseListTask);
        // Existing Judge name means Pronouncement List has already been generated and should be regenerated.
        if (StringUtils.isNotBlank(judgeName)) {
            taskList.add(setFormattedDnCourtDetails);
            taskList.add(documentGenerationTask);
            taskList.add(caseFormatterAddDocuments);
        }
        taskList.add(removePronouncementDetailsTask);

        Task[] taskArr = new Task[taskList.size()];
        return this.execute(
            taskList.toArray(taskArr),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            ImmutablePair.of(DOCUMENT_TYPE, LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, templateId),
            ImmutablePair.of(DOCUMENT_FILENAME, LIST_FOR_PRONOUNCEMENT_FILE_NAME)
        );
    }
}
