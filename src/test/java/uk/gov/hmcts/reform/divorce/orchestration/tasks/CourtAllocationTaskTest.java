package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.SELECTED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@RunWith(MockitoJUnitRunner.class)
public class CourtAllocationTaskTest {

    @Mock
    private CourtAllocator courtAllocator;

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private CourtAllocationTask courtAllocationTask;

    private TaskContext context;

    @Before
    public void setUp() {
        when(courtAllocator.selectCourtForGivenDivorceFact(eq("testReason"))).thenReturn("selectedCourtForReason");
        when(courtAllocator.selectCourtForGivenDivorceFact(isNull())).thenReturn("randomlySelectedCourt");

        context = new DefaultTaskContext();
    }

    @Test
    public void shouldReturnSelectedCourtAsPartOfOutgoingMap_AndCourtInfoIsWrittenToTaskContext() throws TaskException {
        Map incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");
        incomingMap.put(REASON_FOR_DIVORCE_KEY, "testReason");

        Court testCourt = mockCourtLookup("selectedCourtForReason");
        Map<String, Object> outgoingMap = courtAllocationTask.execute(context, incomingMap);

        assertThat(outgoingMap, allOf(
            hasEntry(is("firstKey"), is("firstValue")),
            hasEntry(is(REASON_FOR_DIVORCE_KEY), is("testReason")),
            hasEntry(is(SELECTED_COURT_KEY), is("selectedCourtForReason"))
        ));
        assertThat(context.getTransientObject(SELECTED_COURT), equalTo(testCourt));
    }

    @Test
    public void shouldRandomlySelectCourtEvenWithoutReasonForDivorce_AndCourtInfoIsWrittenToTaskContext() throws TaskException {
        Court testCourt = mockCourtLookup("randomlySelectedCourt");

        Map incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");

        Map<String, Object> outgoingMap = courtAllocationTask.execute(context, incomingMap);

        assertThat(outgoingMap, allOf(
            hasEntry(is("firstKey"), is("firstValue")),
            hasEntry(is(SELECTED_COURT_KEY), is("randomlySelectedCourt"))
        ));
        assertThat(context.getTransientObject(SELECTED_COURT), equalTo(testCourt));
    }

    @Test
    public void shouldOverwriteSelectedCourtFromIncomingMap_AndCourtInfoIsWrittenToTaskContext() throws TaskException {
        Map incomingMap = new HashMap<>();
        incomingMap.put("firstKey", "firstValue");
        incomingMap.put(REASON_FOR_DIVORCE_KEY, "testReason");
        incomingMap.put(SELECTED_COURT_KEY, "previouslySelectedCourt");

        Court testCourt = mockCourtLookup("selectedCourtForReason");
        Map<String, Object> outgoingMap = courtAllocationTask.execute(context, incomingMap);

        assertThat(outgoingMap, allOf(
            hasEntry(is("firstKey"), is("firstValue")),
            hasEntry(is(SELECTED_COURT_KEY), is("selectedCourtForReason"))
        ));
        assertThat(context.getTransientObject(SELECTED_COURT), equalTo(testCourt));
    }

    private Court mockCourtLookup(String courtId) throws TaskException {
        Court testCourt = new Court();
        testCourt.setCourtId(courtId);

        when(taskCommons.getCourt(courtId)).thenReturn(testCourt);

        return testCourt;
    }

}