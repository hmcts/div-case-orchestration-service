package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class FieldsRemovalTaskTest {

    @Test
    public void executeShouldRemoveProvidedListOfFields() {
        Map<String, Object> caseData = buildCaseData();

        Map<String, Object> returnedCaseData = testExecute(caseData, new ArrayList<>(caseData.keySet()), 0);

        assertThat(returnedCaseData.size(), is(not(caseData)));
    }

    @Test
    public void executeShouldExecuteButWithoutRemovingFieldsWhenTheyDontExist() {
        Map<String, Object> caseData = buildCaseData();

        Map<String, Object> returnedCaseData = testExecute(caseData, asList("x", "y", "z"), caseData.size());

        assertThat(returnedCaseData, is(caseData));
    }

    @Test
    public void executeShouldExecuteWithEmptyListOfFieldsToRemove() {
        Map<String, Object> caseData = buildCaseData();

        Map<String, Object> returnedCaseData = testExecute(caseData, emptyList(), caseData.size());

        assertThat(returnedCaseData, is(caseData));
    }

    private Map<String, Object> testExecute(Map<String, Object> caseData, List<String> fields, int expectedSize) {
        FieldsRemovalTask task = new FieldsRemovalTask() {
            @Override
            protected List<String> getFieldsToRemove() {
                return fields;
            }
        };

        Map<String, Object> returnedCaseData = task.execute(contextWithToken(), caseData);

        assertThat(returnedCaseData.size(), is(expectedSize));

        return returnedCaseData;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("a", 1);
        caseData.put("b", 2);
        caseData.put("c", 3);

        return caseData;
    }
}