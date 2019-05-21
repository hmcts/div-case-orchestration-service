package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseIdFromCaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow.LINK_BULK_CASE_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class LinkBulkCaseWorkflowUTest {

    @InjectMocks
    private LinkBulkCaseWorkflow classToTest;

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @Mock
    private GetCaseIdFromCaseLink getCaseIDFromCaseLink;

    @Test
    public void whenGetCase_thenProcessAsExpected() throws WorkflowException {
        Map<String, Object>  data = Collections.emptyMap();

        final Task[] tasks = new Task[]{
            getCaseIDFromCaseLink,
            updateCaseInCCD
        };

        Map<String, Object>  expectedResponse = ImmutableMap.of("someKey", "someValue");
        when(classToTest.execute(tasks, data, ImmutablePair.of(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN),
            ImmutablePair.of(BULK_LISTING_CASE_ID_FIELD, TEST_CASE_ID),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, LINK_BULK_CASE_EVENT))).thenReturn(expectedResponse);

        Map<String, Object> actual = classToTest.run(data, TEST_CASE_ID, AUTH_TOKEN);


        assertEquals(expectedResponse, actual);
    }
}
