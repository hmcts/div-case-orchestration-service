package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    private final IdamClient idamClient;
    private final RestService restService;

    public void assignCaseAccess(CaseDetails caseDetails, String authorisationToken) {
        String userId = idamClient.getUserDetails(authorisationToken).getId();

        AssignCaseAccessRequest assignCaseAccessRequest = buildAssignCaseAccessRequest(caseDetails, userId);

        restService.restApiPostCall(
            authorisationToken,
            assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl(),
            assignCaseAccessRequest
        );
    }

    private AssignCaseAccessRequest buildAssignCaseAccessRequest(CaseDetails caseDetails, String userId) {
        return AssignCaseAccessRequest
            .builder()
            .caseId(caseDetails.getCaseId())
            .assigneeId(userId)
            .caseTypeId(CASE_TYPE_ID)
            .build();
    }
}
