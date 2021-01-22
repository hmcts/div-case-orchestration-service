package uk.gov.hmcts.reform.divorce.orchestration.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUsers;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;


import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveUserRolesRequestMapper {
    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(CaseDetails caseDetails, String userId, String caseRole) {
        return RemoveUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(CaseUsers.builder()
                    .case_id(caseDetails.getCaseId())
                    .user_id(userId)
                    .case_role(caseRole)
                    .build()))
            .build();
    }
}

