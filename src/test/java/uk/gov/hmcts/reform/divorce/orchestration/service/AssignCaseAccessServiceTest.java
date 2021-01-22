package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AssignCaseAccessServiceTest {

    @Mock
    private AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @Mock
    private IdamClient idamClient;

    @Mock
    private RestService restService;

    @InjectMocks
    private AssignCaseAccessService classUnderTest;

    @Test
    public void assignCaseAccessShouldCallAllServicesWithExpectedValues() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id(TEST_USER_ID).build());
        when(assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()).thenReturn(TEST_URL);

        classUnderTest.assignCaseAccess(CaseDetails.builder().caseId(TEST_CASE_ID).build(), AUTH_TOKEN);

        verify(restService).restApiPostCall(
            eq(AUTH_TOKEN),
            eq(TEST_URL),
            eq(
                AssignCaseAccessRequest
                    .builder()
                    .caseId(TEST_CASE_ID)
                    .assigneeId(TEST_USER_ID)
                    .caseTypeId(CASE_TYPE_ID)
                    .build()
            )
        );
    }
}
