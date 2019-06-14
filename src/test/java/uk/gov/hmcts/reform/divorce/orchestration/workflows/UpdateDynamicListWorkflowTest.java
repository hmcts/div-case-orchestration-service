package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNcostOptions;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COST_OPTIONS_DN;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDynamicListWorkflowTest {

    @InjectMocks
    UpdateDynamicListWorkflow updateDynamicListWorkflow;

    @Mock
    SetDNcostOptions setDNcostOptions;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> caseData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        when(setDNcostOptions.execute(context, caseData)).thenReturn(caseData);

        assertEquals(caseData, updateDynamicListWorkflow.run(caseDetails, DIVORCE_COST_OPTIONS_DN));

        verify(setDNcostOptions).execute(context, caseData);
    }
}
