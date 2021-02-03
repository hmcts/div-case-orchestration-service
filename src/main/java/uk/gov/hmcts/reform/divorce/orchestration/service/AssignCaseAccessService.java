package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.AssignCaseAccessClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final AuthTokenGenerator authTokenGenerator;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final IdamClient idamClient;

    public void assignCaseAccess(CaseDetails caseDetails, String authorisationToken) {
        String userId = idamClient.getUserDetails(authorisationToken).getId();

        log.info("CaseId: {} assigning case access to user {}", caseDetails.getCaseId(), userId);

        assignCaseAccessClient.assignCaseAccess(
            authorisationToken,
            authTokenGenerator.generate(),
            buildAssignCaseAccessRequest(caseDetails, userId)
        );

        log.info("CaseId: {} assigned case access to user {}", caseDetails.getCaseId(), userId);
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
