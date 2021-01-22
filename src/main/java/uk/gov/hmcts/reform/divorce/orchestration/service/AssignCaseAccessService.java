package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    private final AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    private final IdamClient idamClient;
    private final RestService restService;

    public void assignCaseAccess(CaseDetails caseDetails, String authorisationToken) {
        String userId = idamClient.getUserDetails(authorisationToken).getId();

        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper
            .mapToAssignCaseAccessRequest(caseDetails, userId);

        restService.restApiPostCall(
            authorisationToken,
            assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl(),
            assignCaseAccessRequest
        );
    }
}
