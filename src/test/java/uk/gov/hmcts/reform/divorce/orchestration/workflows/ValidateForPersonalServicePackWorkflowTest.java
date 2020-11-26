package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PersonalServiceValidationTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ValidateForPersonalServicePackWorkflowTest {

    @Mock
    PersonalServiceValidationTask personalServiceValidationTask;

    @InjectMocks
    ValidateForPersonalServicePackWorkflow validateForPersonalServicePackWorkflow;

    @Test
    public void testRunExecutesExpectedTasksInOrder() throws WorkflowException, TaskException {
        //given
        Map<String, Object> caseData = Collections.singletonMap("key", "value");
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObjects(new HashMap<>() {
            {
                put(AUTH_TOKEN_JSON_KEY, TEST_TOKEN);
                put(CASE_ID_JSON_KEY, TEST_CASE_ID);
            }
        });

        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        //when
        when(personalServiceValidationTask.execute(context, caseData)).thenReturn(caseData);

        Map<String, Object> response = validateForPersonalServicePackWorkflow.run(request, TEST_TOKEN);

        //then
        assertThat(response, is(caseData));
        InOrder inOrder = inOrder(
            personalServiceValidationTask
        );
        inOrder.verify(personalServiceValidationTask).execute(context, caseData);
    }
}
