package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AOSOfflineTriggerRequestEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AOSOfflineTriggerRequestListenerTest {

    @Mock
    private AosService aosService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AOSOfflineTriggerRequestListener classUnderTest;

    @Test
    public void shouldCallAppropriateServiceWhenListeningToAosOfflineTriggerEvent() throws CaseOrchestrationServiceException {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);

        classUnderTest.onApplicationEvent(new AOSOfflineTriggerRequestEvent(this, TEST_CASE_ID));

        verify(aosService).triggerAosOfflineForCase(AUTH_TOKEN, TEST_CASE_ID);
    }

}