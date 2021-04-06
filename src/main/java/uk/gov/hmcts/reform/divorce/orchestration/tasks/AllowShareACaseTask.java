package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerSolicitorDigital;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowShareACaseTask implements Task<Map<String, Object>> {

    private final AssignCaseAccessService assignCaseAccessService;
    private final CcdDataStoreService ccdDataStoreService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String authToken = getAuthToken(context);
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String caseId = caseDetails.getCaseId();

        boolean petitionerSolicitorIsDigital = isPetitionerSolicitorDigital(caseData);
        if (petitionerSolicitorIsDigital) {
            log.info("CaseId: {}, Assigning case access", caseId);
            try {
                assignCaseAccessService.assignCaseAccess(caseDetails, authToken);
                ccdDataStoreService.removeCreatorRole(caseDetails, authToken);
                log.info("CaseId: {}, Assigning case access successful", caseId);
            } catch (Exception exception) {
                log.error("CaseId: {}, Failed to assign case access: {}", caseId, exception.getMessage(), exception);
                context.setTaskFailed(true);
                context.setTransientObject("AssignCaseAccess_Error",
                    "Problem calling assign case access API to set the [PETSOLICITOR] role to the case");
            }
        } else {
            log.info("CaseId: {}. Petitioner solicitor is not digital, so we will not attempt to allow this case to be shared.", caseId);
        }

        return caseData;
    }
}
