package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AsyncTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TaskContext mockContext;

    @Spy
    private AsyncTask classToTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classToTest, "applicationEventPublisher", applicationEventPublisher);
    }

    @Test
    public void givenListOfEvent_whenExecuteTask_AllEventsArePublished() throws TaskException {
        Map<String, Object> payload = Collections.emptyMap();

        ApplicationEvent applicationEvent = mock(ApplicationEvent.class);

        when(classToTest.getApplicationEvent(mockContext, payload)) .thenReturn(Arrays.asList(applicationEvent, applicationEvent));
        classToTest.execute(mockContext, payload);

        verify(applicationEventPublisher, times(2)).publishEvent(applicationEvent);
    }

    @Test
    public void givenEmptyList_whenExecuteTask_NoEventsIsPublished() throws TaskException {
        Map<String, Object> payload = Collections.emptyMap();

        ApplicationEvent applicationEvent = mock(ApplicationEvent.class);

        when(classToTest.getApplicationEvent(mockContext, payload)).thenReturn(Collections.emptyList());
        classToTest.execute(mockContext, payload);

        verify(applicationEventPublisher, never()).publishEvent(applicationEvent);
    }

    @Test(expected = TaskException.class)
    public void givenException_WhenExecuteTask_ExceptionIsPropagated() throws TaskException {
        Map<String, Object> payload = Collections.emptyMap();

        ApplicationEvent applicationEvent = mock(ApplicationEvent.class);
        when(classToTest.getApplicationEvent(mockContext, payload)).thenThrow(new TaskException("Error executing task"));

        classToTest.execute(mockContext, payload);

        verify(applicationEventPublisher, never()).publishEvent(applicationEvent);
    }
}
