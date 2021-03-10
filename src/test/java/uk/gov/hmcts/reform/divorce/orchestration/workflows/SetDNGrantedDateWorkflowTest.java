package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnPronouncementDetailsTask;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;

@RunWith(MockitoJUnitRunner.class)
public class SetDNGrantedDateWorkflowTest {


    @Mock
    SetDnPronouncementDetailsTask setDnPronouncementDetailsTask;

    @InjectMocks
    SetDNGrantedDateWorkflow setDNGrantedDateWorkflow;

    private TaskContext context;
    private Map<String, Object> testData;
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(testData).build();

        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(setDnPronouncementDetailsTask.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, setDNGrantedDateWorkflow.run(caseDetails.getCaseData()));

        verify(setDnPronouncementDetailsTask).execute(context, testData);
    }
}
