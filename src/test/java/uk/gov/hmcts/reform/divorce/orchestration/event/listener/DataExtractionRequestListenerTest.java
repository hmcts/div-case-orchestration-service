package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.CSVExtractorFactory;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.time.LocalDate;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionRequestListenerTest {

    private static final Status TEST_IMPLEMENTED_STATUS = DA;
    private static final Status TEST_UNIMPLEMENTED_STATUS = AOS;

    @Mock
    private DataExtractionService mockService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private CSVExtractorFactory csvExtractorFactory;

    @InjectMocks
    private DataExtractionRequestListener classUnderTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(csvExtractorFactory.hasCSVExtractorForStatus(TEST_IMPLEMENTED_STATUS)).thenReturn(true);
        when(csvExtractorFactory.hasCSVExtractorForStatus(TEST_UNIMPLEMENTED_STATUS)).thenReturn(false);
    }

    @Test
    public void shouldCallServiceInterface_WhenStatusHasCSVExtractor() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, TEST_IMPLEMENTED_STATUS, LocalDate.now()));

        verify(mockService).extractCasesToFamilyMan(eq(TEST_IMPLEMENTED_STATUS), eq(LocalDate.now()), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldNotCallServiceInterface_WhenStatusHasNoCSVExtractor() throws CaseOrchestrationServiceException {
        classUnderTest.onApplicationEvent(new DataExtractionRequest(this, TEST_UNIMPLEMENTED_STATUS, LocalDate.now()));

        verify(mockService, never()).extractCasesToFamilyMan(any(), any(), any());
    }

    @Test
    public void shouldRethrowExceptionIfServiceThrowsException() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(mockService).extractCasesToFamilyMan(any(), any(), any());

        try {
            classUnderTest.onApplicationEvent(
                new DataExtractionRequest(this, TEST_IMPLEMENTED_STATUS, LocalDate.now())
            );
            fail();
        } catch (RuntimeException exception) {
            assertThat(exception.getMessage(), startsWith("Error extracting data to Family man for status DA and date"));
            assertThat(exception.getCause(), instanceOf(CaseOrchestrationServiceException.class));
        }
    }
}
