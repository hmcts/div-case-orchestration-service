package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDnOutcomeFlagFieldTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetDNOutcomeFlagWorkflowTest {

    @Mock
    private AddDnOutcomeFlagFieldTask addDNOutcomeFlagTask;

    @InjectMocks
    private SetDNOutcomeFlagWorkflow classToTest;


    private Map<String, Object> inputPayload;

    @Before
    public void setUp() {
        inputPayload = new HashMap<>();
    }

    @Test
    public void whenRunWorkflow_thenReturnTaskResponse() throws WorkflowException {
        inputPayload.put("InputKey", "InputValue");
        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        payloadReturnedByTask.put("addedKey", "addedValue");
        when(addDNOutcomeFlagTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = classToTest.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, is(payloadReturnedByTask));
    }
}