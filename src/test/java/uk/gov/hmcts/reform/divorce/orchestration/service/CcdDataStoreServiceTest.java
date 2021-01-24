package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService.CREATOR_CASE_ROLE;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataStoreServiceTest {

    @Mock
    private CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;

    @Mock
    private IdamClient idamClient;

    @Mock
    private RestService restService;

    @InjectMocks
    private CcdDataStoreService ccdDataStoreService;

    @Test
    public void test() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id(TEST_USER_ID).build());
        when(ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl()).thenReturn(TEST_URL);

        ccdDataStoreService.removeCreatorRole(CaseDetails.builder().caseId(TEST_CASE_ID).build(), AUTH_TOKEN);

        verify(restService).restApiDeleteCall(
            AUTH_TOKEN,
            TEST_URL,
            RemoveUserRolesRequest
                .builder()
                .caseUsers(
                    Collections.singletonList(
                        CaseUser.builder()
                            .caseId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(CREATOR_CASE_ROLE)
                            .build()
                    )
                ).build()
        );
    }
}
