package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AOSOfflineTriggerRequestEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AosOfflineTriggerRequestTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AosOfflineTriggerRequestTask aosOfflineTriggerRequestTask;

    @Captor
    private ArgumentCaptor<AOSOfflineTriggerRequestEvent> eventArgumentCaptor;

    @Test
    public void shouldPublishEventForTriggeringAosOverdueForCase() {
        aosOfflineTriggerRequestTask.execute(null, TEST_CASE_ID);

        verify(applicationEventPublisher).publishEvent(eventArgumentCaptor.capture());
        AOSOfflineTriggerRequestEvent publishedEvent = eventArgumentCaptor.getValue();
        assertThat(publishedEvent.getCaseId(), equalTo(TEST_CASE_ID));
        assertThat(publishedEvent.getSource().getClass(), equalTo(AosOfflineTriggerRequestTask.class));
    }

}