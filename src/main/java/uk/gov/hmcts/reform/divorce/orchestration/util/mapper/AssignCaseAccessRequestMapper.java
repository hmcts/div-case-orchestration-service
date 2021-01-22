package uk.gov.hmcts.reform.divorce.orchestration.util.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessRequestMapper {
    public AssignCaseAccessRequest mapToAssignCaseAccessRequest(CaseDetails caseDetails, String userId) {
        return AssignCaseAccessRequest
            .builder()
            .caseId(caseDetails.getCaseId())
            .assigneeId(userId)
            .caseTypeId(CASE_TYPE_ID)
            .build();
    }
}
