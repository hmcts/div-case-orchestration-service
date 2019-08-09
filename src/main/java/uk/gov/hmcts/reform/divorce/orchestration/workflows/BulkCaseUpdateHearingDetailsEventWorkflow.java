package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseHearingDetailsWithinBulk;

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
@AllArgsConstructor
public class BulkCaseUpdateHearingDetailsEventWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final UpdateDivorceCaseHearingDetailsWithinBulk updateDivorceCaseHearingDetailsWithinBulk;
    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;
    private final DocumentGenerationTask documentGenerationTask;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {


        String templateId = "FL-DIV-GNO-ENG-00059.docx";
        String documentType = "caseListForPronouncement";
        String filename = "caseListForPronouncement";

        List<Task> taskList = new ArrayList<>();
        String judgeName = (String) callbackRequest.getCaseDetails().getCaseData().get(PRONOUNCEMENT_JUDGE_CCD_FIELD);
        if (StringUtils.isNotEmpty(judgeName)) {
            taskList.add(setFormattedDnCourtDetails);
            taskList.add(documentGenerationTask);
            taskList.add(caseFormatterAddDocuments);
        }

        taskList.add(updateDivorceCaseHearingDetailsWithinBulk);

        Task[] tasks = new Task[taskList.size()];
        return this.execute(
                taskList.toArray(tasks),
                callbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, callbackRequest.getCaseDetails()),
                ImmutablePair.of(DOCUMENT_TYPE, documentType),
                ImmutablePair.of(DOCUMENT_TEMPLATE_ID, templateId),
                ImmutablePair.of(DOCUMENT_FILENAME, filename)
        );

    }

}