package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATOR_USER_ROLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    private final RemoveUserRolesRequestMapper removeUserRolesRequestMapper;
    private final IdamService idamService;
    private final RestService restService;

    public void removeCreatorRole(CaseDetails caseDetails, String authorisationToken) {
        removeRole(caseDetails, authorisationToken, CREATOR_USER_ROLE);
    }

    private void removeRole(CaseDetails caseDetails, String authorisationToken, String role) {
        String userId = idamService.getIdamUserId(authorisationToken);
        RemoveUserRolesRequest removeUserRolesRequest = removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseDetails, userId, role);

        restService.restApiDeleteCall(
            authorisationToken,
            ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl(),
            removeUserRolesRequest);
    }
}
