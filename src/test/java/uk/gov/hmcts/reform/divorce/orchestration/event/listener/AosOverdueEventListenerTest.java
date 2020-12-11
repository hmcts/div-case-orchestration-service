package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForAlternativeMethodCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForProcessServerCaseEvent;
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
public class AosOverdueEventListenerTest {

    @Mock
    private AosService aosService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AosOverdueEventListener classUnderTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void shouldCallAppropriateServiceWhenAosOverdueEventIsListened() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new AosOverdueEvent(this, TEST_CASE_ID));

        verify(aosService).makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void shouldThrowRuntimeException_WhenServiceThrowsException_ForAosOverdue() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(aosService).makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID);
        AosOverdueEvent aosOverdueEvent = new AosOverdueEvent(this, TEST_CASE_ID);

        RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> classUnderTest.onApplicationEvent(aosOverdueEvent)
        );
        assertThat(runtimeException.getMessage(), is("Error processing AosOverdueEvent event for case " + TEST_CASE_ID));
    }

    @Test
    public void shouldCallAppropriateServiceWhenForProcessServerCaseEventIsListened() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new AosOverdueForProcessServerCaseEvent(this, TEST_CASE_ID));

        verify(aosService).markAosNotReceivedForProcessServerCase(AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void shouldThrowRuntimeException_WhenServiceThrowsException_ForAosOverdueForProcessServerCase() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(aosService).markAosNotReceivedForProcessServerCase(AUTH_TOKEN, TEST_CASE_ID);
        AosOverdueForProcessServerCaseEvent event = new AosOverdueForProcessServerCaseEvent(this, TEST_CASE_ID);

        RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> classUnderTest.onApplicationEvent(event)
        );
        assertThat(runtimeException.getMessage(), is("Error processing AosOverdueForProcessServerCaseEvent event for case " + TEST_CASE_ID));
    }

    @Test
    public void shouldCallAppropriateServiceWhenForAlternativeMethodCaseEventIsListened() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new AosOverdueForAlternativeMethodCaseEvent(this, TEST_CASE_ID));

        verify(aosService).markAosNotReceivedForAlternativeMethodCase(AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void shouldThrowRuntimeException_WhenServiceThrowsException_ForAosOverdueForAlternativeMethod() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(aosService).markAosNotReceivedForAlternativeMethodCase(AUTH_TOKEN, TEST_CASE_ID);
        AosOverdueForAlternativeMethodCaseEvent event = new AosOverdueForAlternativeMethodCaseEvent(this, TEST_CASE_ID);

        RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> classUnderTest.onApplicationEvent(event)
        );
        assertThat(runtimeException.getMessage(), is("Error processing AosOverdueForAlternativeMethodCaseEvent event for case " + TEST_CASE_ID));
    }

}