package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

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

        log.info("Assigning case access for Case ID: {}", caseId);
        ccdDataStoreService.removeCreatorRole(caseDetails, authToken);
        assignCaseAccessService.assignCaseAccess(caseDetails, authToken);

        return caseData;
    }
}

