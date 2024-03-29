package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.AssignCaseAccessClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AssignCaseAccessServiceTest {

    public static final boolean USE_USER_TOKEN_PARAMETER = true;
    public static final boolean DONT_USE_USER_TOKEN_PARAMETER = false;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private AssignCaseAccessService classUnderTest;

    @Test
    public void assignCaseAccessShouldCallAllServicesWithExpectedValuesAndUseUserTokenParamTrue() {
        executeAndAssert(USE_USER_TOKEN_PARAMETER);
    }

    @Test
    public void assignCaseAccessShouldCallAllServicesWithExpectedValuesAndUseUserTokenParamFalse() {
        executeAndAssert(DONT_USE_USER_TOKEN_PARAMETER);
    }

    private void executeAndAssert(boolean useUserTokenFlag) {
        when(featureToggleService.isFeatureEnabled(Features.USE_USER_TOKEN)).thenReturn(useUserTokenFlag);
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id(TEST_USER_ID).build());
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        classUnderTest.assignCaseAccess(CaseDetails.builder().caseId(TEST_CASE_ID).build(), AUTH_TOKEN);

        verify(assignCaseAccessClient).assignCaseAccess(
            AUTH_TOKEN,
            TEST_SERVICE_TOKEN,
            useUserTokenFlag,
            AssignCaseAccessRequest
                .builder()
                .caseId(TEST_CASE_ID)
                .assigneeId(TEST_USER_ID)
                .caseTypeId(CASE_TYPE_ID)
                .build()
        );
    }
}
