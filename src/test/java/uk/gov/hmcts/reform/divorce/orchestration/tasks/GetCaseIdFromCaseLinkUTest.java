package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseIdFromCaseLinkUTest {

    @InjectMocks
    private GetCaseIdFromCaseLink classToTest;

    @Test(expected = TaskException.class)
    public void givenCaseLinkWithoutCaseReference_thenThrowTaskException() throws TaskException {
        classToTest.execute(new DefaultTaskContext(), Collections.emptyMap());

    }

    @Test
    public void givenCaseLin_thenReturnBulkCaseId() throws TaskException {
        final String bulkCaseId = "BulkCaseId";
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(BULK_LISTING_CASE_ID_FIELD, bulkCaseId);
        Map<String ,Object> caseData = new HashMap<>();
        caseData.put(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD,
            ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID)));
        Map<String, Object> result = classToTest.execute(context, caseData);

        assertEquals(context.getTransientObject(CASE_ID_JSON_KEY), TEST_CASE_ID);
        assertTrue(result.containsKey(BULK_LISTING_CASE_ID_FIELD));

    }

}
