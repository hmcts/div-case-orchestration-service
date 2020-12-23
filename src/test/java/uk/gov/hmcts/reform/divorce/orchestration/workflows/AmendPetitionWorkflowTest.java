package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AmendPetitionWorkflowTest {

    @Mock
    private CreateAmendPetitionDraftTask amendPetitionDraft;

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @Mock
    private AddCourtsToPayloadTask addCourtsToPayloadTask;

    @InjectMocks
    @Spy
    private AmendPetitionWorkflow classUnderTest;

    @Test
    public void shouldCallRightTasks() throws WorkflowException, TaskException {
        when(amendPetitionDraft.execute(any(), any())).then(invocationOnMock -> {
            Arrays.stream(invocationOnMock.getArguments())
                .filter(o -> o instanceof TaskContext)
                .map(TaskContext.class::cast)
                .findFirst()
                .ifPresent(c -> c.setTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY, singletonMap("draftKey", "draftValue")));
            return emptyMap();
        });
        when(addCourtsToPayloadTask.execute(any(), any())).then(invocationOnMock -> {
            Map mapParameter = Arrays.stream(invocationOnMock.getArguments())
                .filter(o -> o instanceof Map)
                .map(Map.class::cast)
                .findFirst()
                .orElse(emptyMap());

            Map mapToReturn = new HashMap<>(mapParameter);
            mapToReturn.put("courtList", "allCourts");

            return mapToReturn;
        });

        Map<String, Object> returnedPayload = classUnderTest.run("testCaseId", "testToken");

        assertThat(returnedPayload, allOf(
            hasEntry("draftKey", "draftValue"),
            hasEntry("courtList", "allCourts")
        ));
        verify(classUnderTest).execute(aryEq(new Task[] {
            amendPetitionDraft,
            updateCaseInCCD
        }), any(Map.class), any(), any(), any());
    }

}