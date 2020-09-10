package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;

public class FieldsRemovalTaskTest {

    @Test
    public void executeShouldRemoveProvidedListOfFields() {
        Map<String, Object> caseData = buildCaseData();

        FieldsRemovalTask task = new FieldsRemovalTask() {
            @Override
            protected List<String> getFieldsToRemove() {
                return new ArrayList<>(caseData.keySet());
            }
        };

        Map<String, Object> returnedCaseData = task.execute(prepareTaskContext(), caseData);

        assertThat(returnedCaseData.size(), is(0));
        assertThat(returnedCaseData.size(), is(not(caseData)));
    }

    @Test
    public void executeShouldExecuteButWithoutRemovingFieldsWhenTheyDontExist() {
        Map<String, Object> caseData = buildCaseData();

        FieldsRemovalTask task = new FieldsRemovalTask() {
            @Override
            protected List<String> getFieldsToRemove() {
                return asList("x", "y", "z");
            }
        };

        Map<String, Object> returnedCaseData = task.execute(prepareTaskContext(), caseData);

        assertThat(returnedCaseData.size(), is(3));
        assertThat(returnedCaseData, is(caseData));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("a", 1);
        caseData.put("b", 2);
        caseData.put("c", 3);

        return caseData;
    }
}