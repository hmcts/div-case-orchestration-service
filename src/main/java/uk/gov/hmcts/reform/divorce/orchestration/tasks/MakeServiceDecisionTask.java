package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeServiceDecisionTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        /*
        AC1
            GIVEN a case is in the state 'awaitingServiceConsideration'
            WHEN a legal advisor triggers the event 'makeServiceDecision'
            THEN the 'Approve service application' screen is shown
            AND serviceApplicationGranted is a mandatory field
         */

        if (isAwaitingServiceConsideration(caseData)){
            // THEN the 'Approve service application' screen is shown
        }

        /*
        AC3
            GIVEN that serviceApplicationGranted is 'Yes'
            WHEN a legal advisor clicks on 'continue'
            THEN the case state is changed to 'AwaitingDNApplication"
         */
        if (isServiceApplicationGranted(caseData)) {
            // THEN the case state is changed to 'AwaitingDNApplication"
        }

        log.info("CaseID: {}, State {} sent.", getCaseId(context));

        return caseData;
    }

    private static boolean isAwaitingServiceConsideration(Map<String, Object> caseData) {
        String state = (String) caseData.get(STATE_CCD_FIELD);
        return state.equalsIgnoreCase("awaitingServiceConsideration") ? true: false;
    }

    private static boolean isServiceApplicationGranted(Map<String, Object> caseData) {
        String state = (String) caseData.get(STATE_CCD_FIELD);
        return state.equalsIgnoreCase("serviceApplicationGranted") ? true: false;
    }
}
