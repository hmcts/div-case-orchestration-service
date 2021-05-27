package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

public class RemoveDnOutcomeCaseFlagTaskTest {

    private final RemoveDnOutcomeCaseFlagTask classToTest = new RemoveDnOutcomeCaseFlagTask();

    @Test
    public void testExecuteTaskRemoveDnOutcomeCaseFlag() throws TaskException {
        TaskContext context = context();
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("anyKey", "anyData");
        caseData.put(CcdFields.DN_OUTCOME_FLAG,"Yes");

        Map<String, Object> response = classToTest.execute(context, caseData);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("anyKey", "anyData");
        assertThat(response, is(expectedMap));
    }
}