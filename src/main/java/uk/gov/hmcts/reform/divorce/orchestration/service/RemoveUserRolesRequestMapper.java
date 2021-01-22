package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class RemoveUserRolesRequestMapper {
    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(
        CaseDetails caseDetails, String userId, String caseRole) {
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
