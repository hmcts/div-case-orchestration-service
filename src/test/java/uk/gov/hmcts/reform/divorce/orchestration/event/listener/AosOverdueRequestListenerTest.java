package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AosOverdueRequestListenerTest {

    @Mock
    private AosService aosService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AosOverdueRequestListener classUnderTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void shouldCallServiceWhenRequestIsListened() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new AosOverdueRequest(this, TEST_CASE_ID));

        verify(aosService).makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void shouldThrowRuntimeException_WhenServiceThrowsException() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(aosService).makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID);
        AosOverdueRequest aosOverdueRequest = new AosOverdueRequest(this, TEST_CASE_ID);

        RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> classUnderTest.onApplicationEvent(aosOverdueRequest)
        );
        assertThat(runtimeException.getMessage(), is("Error trying to move case " + TEST_CASE_ID + " to AOS Overdue state"));
    }
}
