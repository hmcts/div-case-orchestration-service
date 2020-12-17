package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.Getter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SelfPublishingAsyncTaskTest {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;

    @InjectMocks
    private TestSelfPublishingAsyncTask classUnderTest;

    @Captor
    private ArgumentCaptor<TestApplicationEvent> eventCaptor;

    @Test
    public void shouldSelfPublishEvents() {
        List<String> incomingPayload = List.of("This should be published", "This should also be published");

        List<String> returnedPayload = classUnderTest.execute(null, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(mockEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<TestApplicationEvent> allValues = eventCaptor.getAllValues();
        assertThat(allValues.get(0).getText(), is("This should be published"));
        assertThat(allValues.get(1).getText(), is("This should also be published"));
    }

    private static class TestSelfPublishingAsyncTask extends SelfPublishingAsyncTask<List<String>> {

        @Override
        protected void publishApplicationEvents(TaskContext context,
                                                List<String> payload,
                                                Consumer<? super ApplicationEvent> eventPublishingFunction) {
            payload.stream()
                .map(message -> new TestApplicationEvent(this, message))
                .forEach(eventPublishingFunction);
        }

    }

    @Getter
    private static class TestApplicationEvent extends ApplicationEvent {
        private final String text;

        public TestApplicationEvent(Object source, String text) {
            super(source);
            this.text = text;
        }
    }

}