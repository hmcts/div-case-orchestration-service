package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnGrantedDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCasePronouncementDateWithinBulk;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@AllArgsConstructor
public class BulkCaseUpdateDnPronounceDatesWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ObjectMapper objectMapper;
    private final SetDnGrantedDate setDnGrantedDate;
    private final UpdateDivorceCasePronouncementDateWithinBulk updateDivorceCasePronouncementDateWithinBulk;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {

        Map<String, Object> bulkCaseDetailsAsMap = objectMapper.convertValue(caseDetails, Map.class);

        return this.execute(
                new Task[] {
                    setDnGrantedDate,
                    updateDivorceCasePronouncementDateWithinBulk
                },
                caseDetails.getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(BULK_CASE_DETAILS_CONTEXT_KEY, bulkCaseDetailsAsMap)
        );
    }
}
