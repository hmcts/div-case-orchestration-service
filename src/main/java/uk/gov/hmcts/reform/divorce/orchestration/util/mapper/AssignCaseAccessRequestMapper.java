package uk.gov.hmcts.reform.divorce.orchestration.util.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.shared.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;


@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessRequestMapper {
    public AssignCaseAccessRequest mapToAssignCaseAccessRequest(CaseDetails caseDetails, String userId) {
        return AssignCaseAccessRequest
            .builder()
            .case_id(caseDetails.getCaseId())
            .assignee_id(userId)
            .case_type_id(caseDetails.getCaseTypeId())
            .build();
    }
}
