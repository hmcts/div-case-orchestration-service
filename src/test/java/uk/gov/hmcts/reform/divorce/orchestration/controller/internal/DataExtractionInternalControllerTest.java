package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.listener.DataExtractionRequestListener;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionInternalControllerTest {

    @Mock
    private DataExtractionRequestListener listener;

    @Captor
    private ArgumentCaptor<DataExtractionRequest> dataExtractionRequestArgumentCaptor;

    @InjectMocks
    private DataExtractionInternalController classUnderTest;

    @Test
    public void shouldCallJob_ForFamilyManDataExtraction() {
        classUnderTest.startDataExtractionToFamilyMan();

        verify(listener).onApplicationEvent(dataExtractionRequestArgumentCaptor.capture());
        DataExtractionRequest event = dataExtractionRequestArgumentCaptor.getValue();
        assertThat(event.getStatus(), is(DA));
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThat(event.getDate(), is(yesterday));
    }

}