package uk.gov.hmcts.reform.divorce.orchestration.controller;

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

import static java.time.Month.AUGUST;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionControllerTest {

    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @Mock
    private DataExtractionRequestListener listener;

    @Captor
    private ArgumentCaptor<DataExtractionRequest> dataExtractionRequestArgumentCaptor;

    @InjectMocks
    private DataExtractionController classUnderTest;

    @Test
    public void shouldCallJob_ForFamilyManDataExtraction() {
        classUnderTest.startDataExtractionToFamilyMan();

        verify(listener, times(3)).onApplicationEvent(dataExtractionRequestArgumentCaptor.capture());
        List<DataExtractionRequest> allEvents = dataExtractionRequestArgumentCaptor.getAllValues();

        verifyEvent(allEvents.get(0), DA);
        verifyEvent(allEvents.get(1), AOS);
        verifyEvent(allEvents.get(2), DN);
    }

    @Test
    public void shouldCallListenerWithGivenParameters() {
        LocalDate lastModifiedDate = LocalDate.of(2019, AUGUST, 12);
        classUnderTest.startDataExtractionToFamilyManForGivenStatusAndDate(DA, lastModifiedDate);

        verify(listener).onApplicationEvent(dataExtractionRequestArgumentCaptor.capture());
        DataExtractionRequest dataExtractionRequest = dataExtractionRequestArgumentCaptor.getValue();
        assertThat(dataExtractionRequest.getSource(), is(instanceOf(DataExtractionController.class)));
        assertThat(dataExtractionRequest.getStatus(), is(DA));
        assertThat(dataExtractionRequest.getDate(), is(lastModifiedDate));
    }

    private void verifyEvent(DataExtractionRequest event, Status expectedStatus) {
        assertThat(event.getDate(), is(yesterday));
        assertThat(event.getStatus(), is(expectedStatus));
    }

}