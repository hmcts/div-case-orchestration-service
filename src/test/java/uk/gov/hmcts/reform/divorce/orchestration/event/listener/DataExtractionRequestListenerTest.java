package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.time.LocalDate;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionRequestListenerTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private DataExtractionService mockService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private DataExtractionRequestListener classUnderTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(TEST_AUTH_TOKEN);
    }

    @Test
    public void shouldCallServiceInterface_ForDAStatus() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, DA, LocalDate.now()));

        verify(mockService).extractCasesToFamilyMan(eq(DA), eq(LocalDate.now()), eq(TEST_AUTH_TOKEN));
    }

    @Test
    public void shouldNotCallServiceInterface_ForAOSStatus() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, AOS, LocalDate.now()));

        verify(mockService, never()).extractCasesToFamilyMan(any(), any(), any());
    }

    @Test
    public void shouldNotCallServiceInterface_ForDNStatus() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, DN, LocalDate.now()));

        verify(mockService, never()).extractCasesToFamilyMan(any(), any(), any());
    }

    @Test
    public void shouldRethrowExceptionIfServiceThrowsException() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(mockService).extractCasesToFamilyMan(any(), any(), any());

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Error extracting data to Family man");
        expectedException.expectCause(instanceOf(CaseOrchestrationServiceException.class));

        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, DA, LocalDate.now()));
    }

}