package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseRoleClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    public static final String CREATOR_CASE_ROLE = "[CREATOR]";

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final CaseRoleClient caseRoleClient;

    public void removeCreatorRole(CaseDetails caseDetails, String authorisationToken) {
        removeRole(caseDetails, authorisationToken, CREATOR_CASE_ROLE);
        removeRole(caseDetails, authorisationToken, "[PETSOLICITOR]");
    }

    private void removeRole(CaseDetails caseDetails, String authorisationToken, String caseRole) {
        String userId = idamClient.getUserDetails(authorisationToken).getId();

        log.info("CaseId: {} removing case role {} from user {}", caseDetails.getCaseId(), caseRole, userId);

        caseRoleClient.removeCaseRoles(
            authorisationToken,
            authTokenGenerator.generate(),
            buildRemoveUserRolesRequest(caseDetails, caseRole, userId)
        );

        log.info("CaseId: {} removed case role {} from user {}", caseDetails.getCaseId(), caseRole, userId);
    }

    private RemoveUserRolesRequest buildRemoveUserRolesRequest(
        CaseDetails caseDetails, String caseRole, String userId) {
        return RemoveUserRolesRequest
            .builder()
            .caseUsers(
                Collections.singletonList(
                    CaseUser.builder()
                        .caseId(caseDetails.getCaseId())
                        .userId(userId)
                        .caseRole(caseRole)
                        .build()
                )
            ).build();
    }
}
