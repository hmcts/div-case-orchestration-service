package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.event.listener.DataExtractionRequestListener;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionInternalControllerTest {

    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @Mock
    private DataExtractionRequestListener listener;

    @Captor
    private ArgumentCaptor<DataExtractionRequest> dataExtractionRequestArgumentCaptor;

    @InjectMocks
    private DataExtractionInternalController classUnderTest;

    @Test
    public void shouldCallJob_ForFamilyManDataExtraction() {
        classUnderTest.startDataExtractionToFamilyMan();

        verify(listener, times(3)).onApplicationEvent(dataExtractionRequestArgumentCaptor.capture());
        List<DataExtractionRequest> allEvents = dataExtractionRequestArgumentCaptor.getAllValues();

        verifyEvent(allEvents.get(0), DA);
        verifyEvent(allEvents.get(1), AOS);
        verifyEvent(allEvents.get(2), DN);
    }

    private void verifyEvent(DataExtractionRequest event, Status expectedStatus) {
        assertThat(event.getDate(), is(yesterday));
        assertThat(event.getStatus(), is(expectedStatus));
    }

}