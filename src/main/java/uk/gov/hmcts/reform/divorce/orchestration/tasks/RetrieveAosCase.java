package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_STATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.COURT_KEY;

@Component
@Slf4j
public class RetrieveAosCase implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public RetrieveAosCase(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );

        if (caseDetails == null) {
            throw new TaskException(new CaseNotFoundException("No case found"));
        }

        log.info("---* Case data retrieve AOS: {}", caseDetails.getCaseData());

        Map<String, Object> caseData = caseDetails.getCaseData();
        context.setTransientObject(CASE_ID_KEY, caseDetails.getCaseId());
        context.setTransientObject(CASE_STATE_KEY, caseDetails.getState());
        context.setTransientObject(COURT_KEY, String.valueOf(caseData.get(D_8_DIVORCE_UNIT)));

        return caseData;
    }

}