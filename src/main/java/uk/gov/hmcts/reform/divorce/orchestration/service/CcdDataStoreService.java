package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseRoleClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseRoles;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request.UpdateUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final CaseRoleClient caseRoleClient;

    public void removeCreatorRole(CaseDetails caseDetails, String authorisationToken) {
        removeRoles(caseDetails, authorisationToken);
    }

    public void addPetitionerSolicitorRole(CaseDetails caseDetails, String authorisationToken) {
        addCaseRole(caseDetails, authorisationToken, CaseRoles.PETITIONER_SOLICITOR);
    }

    private void addCaseRole(CaseDetails caseDetails, String authorisationToken, String caseRole) {
        UserDetails userDetails = idamClient.getUserDetails(authorisationToken);
        String userId = userDetails.getId();
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} adding case role {} to user {}", caseId, caseRole, userId);

        caseRoleClient.updateCaseRolesForUser(
            authorisationToken,
            authTokenGenerator.generate(),
            caseId,
            userId,
            buildUpdateUserRolesRequest(caseId, userId, caseRole)
        );

        log.info("CaseID: {} added case role {} to user {}", caseId, caseRole, userId);
    }

    private void removeRoles(CaseDetails caseDetails, String authorisationToken) {
        UserDetails userDetails = idamClient.getUserDetails(authorisationToken);
        String userId = userDetails.getId();
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} removing [CREATOR] and [PETSOLICITOR] case roles from user {}", caseId, userId);

        caseRoleClient.removeCaseRoles(
            authorisationToken,
            authTokenGenerator.generate(),
            buildRemoveUserRolesRequest(caseId, userId)
        );

        log.info("CaseID: {} removed  [CREATOR] and [PETSOLICITOR] case roles from user {}", caseId, userId);
    }

    private UpdateUserRolesRequest buildUpdateUserRolesRequest(String caseId, String userId, String caseRole) {
        return UpdateUserRolesRequest
            .builder()
            .caseId(caseId)
            .caseRoles(asList(caseRole))
            .build();
    }

    private RemoveUserRolesRequest buildRemoveUserRolesRequest(String caseId, String userId) {
        return RemoveUserRolesRequest
            .builder()
            .caseUsers(getCaseUsers(caseId, userId))
            .build();
    }

    private CaseUser buildCaseUser(String caseId, String caseRole, String userId) {
        return CaseUser.builder()
            .caseId(caseId)
            .userId(userId)
            .caseRole(caseRole)
            .build();
    }

    private List<CaseUser> getCaseUsers(String caseId, String userId) {
        return asList(
            buildCaseUser(caseId, CaseRoles.CREATOR, userId),
            buildCaseUser(caseId, CaseRoles.PETITIONER_SOLICITOR, userId)
        );
    }
}
