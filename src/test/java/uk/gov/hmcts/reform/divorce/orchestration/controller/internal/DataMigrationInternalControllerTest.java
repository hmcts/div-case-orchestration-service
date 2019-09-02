package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.listener.DataMigrationRequestListener;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest.Status.DA;

@RunWith(MockitoJUnitRunner.class)
public class DataMigrationInternalControllerTest {

    @Mock
    private DataMigrationRequestListener listener;

    @Captor
    private ArgumentCaptor<DataMigrationRequest> dataMigrationRequestArgumentCaptor;

    @InjectMocks
    private DataMigrationInternalController classUnderTest;

    @Test
    public void shouldCallJob_ForFamilyManDataMigration() {
        classUnderTest.startFamilyManDataMigration();

        verify(listener).onApplicationEvent(dataMigrationRequestArgumentCaptor.capture());
        DataMigrationRequest event = dataMigrationRequestArgumentCaptor.getValue();
        assertThat(event.getStatus(), is(DA));
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThat(event.getDate(), is(yesterday));
    }

}