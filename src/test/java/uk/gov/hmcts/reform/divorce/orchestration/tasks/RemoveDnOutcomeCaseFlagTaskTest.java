package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;

public class RemoveDnOutcomeCaseFlagTaskTest {

    private RemoveDnOutcomeCaseFlagTask classToTest = new RemoveDnOutcomeCaseFlagTask();

    @Test
    public void testExecuteTaskRemoveDnOutcomeCaseFlag() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of("anyKey", "anyData",
                DN_OUTCOME_FLAG_CCD_FIELD,"Yes");
        Map<String, Object> response = classToTest.execute(null, caseData);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("anyKey", "anyData");
        assertThat(response, is(expectedMap));
    }
}