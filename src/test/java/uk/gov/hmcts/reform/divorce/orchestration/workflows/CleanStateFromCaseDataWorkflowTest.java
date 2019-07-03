package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class CleanStateFromCaseDataWorkflowTest {

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private CleanStateFromCaseDataWorkflow classToTest;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> resultData = Collections.singletonMap("Hello", "World");

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(STATE_CCD_FIELD, null);
        when(updateCaseInCCD.execute(any(), eq(caseData))).thenReturn(resultData);
        Map<String, Object> response = classToTest.run(TEST_CASE_ID, AUTH_TOKEN);

        assertThat(response, is(resultData));
    }
}