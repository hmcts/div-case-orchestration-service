package uk.gov.hmcts.reform.divorce.orchestration.tasks;

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

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

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
        Map<String ,Object> caseDetails = new HashMap<>();
        caseDetails.put(ID, TEST_CASE_ID);

        Map<String, Object> result = classToTest.execute(context, caseDetails);

        assertTrue(result.containsKey(BULK_LISTING_CASE_ID_FIELD));

    }

}
