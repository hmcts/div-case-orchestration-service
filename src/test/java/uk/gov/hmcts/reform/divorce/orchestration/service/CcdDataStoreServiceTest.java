package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseRoleClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataStoreServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CaseRoleClient caseRoleClient;

    @InjectMocks
    private CcdDataStoreService ccdDataStoreService;

    @Test
    public void removeCreatorRoleCallsApi() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id(TEST_USER_ID).build());
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        ccdDataStoreService.removeCreatorRole(CaseDetails.builder().caseId(TEST_CASE_ID).build(), AUTH_TOKEN);

        verify(caseRoleClient).removeCaseRoles(
            AUTH_TOKEN,
            TEST_SERVICE_TOKEN,
            RemoveUserRolesRequest
                .builder()
                .caseUsers(
                    Collections.singletonList(
                        CaseUser.builder()
                            .caseId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(null)
                            .build()
                    )
                ).build()
        );
    }
}
