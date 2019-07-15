package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCaseTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisherMock;

    @InjectMocks
    private UpdateDNPronouncedCase classToTest;

    @Test
    public void givenListCase_thenPublishEvents() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        String[] caseIds = {"someId1", "someId2"};
        context.setTransientObject(SEARCH_RESULT_KEY, Arrays.asList(caseIds));
        Map<String, Object> payload = new HashMap<>();

        classToTest.execute(context, payload);

        verify(applicationEventPublisherMock, times(2)).publishEvent(any());
    }

    @Test
    public void givenEmptyMap_whenGetEvents_thenReturnEmptyList() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(SEARCH_RESULT_KEY, Collections.emptyList());
        Map<String, Object> payload = new HashMap<>();

        assertEquals(Collections.emptyList(), classToTest.getApplicationEvent(context, payload));
    }

    @Test
    public void givenListCase_thenReturnEventList() {
        TaskContext context = new DefaultTaskContext();
        String[] caseIds = {"someId1", "someId2"};
        context.setTransientObject(SEARCH_RESULT_KEY, Arrays.asList(caseIds));
        Map<String, Object> payload = new HashMap<>();

        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);

        assertEquals(2, result.size());
    }
}
