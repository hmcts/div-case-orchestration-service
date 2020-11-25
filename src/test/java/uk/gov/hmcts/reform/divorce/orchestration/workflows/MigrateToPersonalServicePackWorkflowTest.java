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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtServiceValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MigrateCaseToPersonalServiceTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_SERVICE_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class MigrateToPersonalServicePackWorkflowTest {

    @Mock
    CourtServiceValidationTask courtServiceValidationTask;

    @Mock
    MigrateCaseToPersonalServiceTask migrateCaseToPersonalServiceTask;

    @InjectMocks
    MigrateToPersonalServicePackWorkflow migrateToPersonalServicePackWorkflow;

    DefaultTaskContext context;

    CaseDetails caseDetails;

    CcdCallbackRequest request;

    //given
    Map<String, Object> caseData;

    public void setupTest(String state, Map<String, Object> caseData) {
        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(state)
            .caseData(caseData)
            .build();

        context = new DefaultTaskContext();

        context.setTransientObjects(new HashMap<String, Object>() {
            {
                put(AUTH_TOKEN_JSON_KEY, TEST_TOKEN);
                put(CASE_ID_JSON_KEY, TEST_CASE_ID);
                put(CASE_DETAILS_JSON_KEY, caseDetails);
            }
        });

        request = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        //when
        when(courtServiceValidationTask.execute(context, caseData)).thenReturn(caseData);
        when(migrateCaseToPersonalServiceTask.execute(context, caseData)).thenReturn(caseData);
    }

    @Test
    public void testRunExecutesExpectedTasksInOrderForCaseStateAosOverdue() throws WorkflowException, TaskException {
        caseData = Collections.singletonMap("SolServiceMethod", COURT_SERVICE_VALUE);
        setupTest(AOS_OVERDUE, caseData);

        Map<String, Object> response = migrateToPersonalServicePackWorkflow.run(request, TEST_TOKEN);

        //then
        assertThat(response, is(caseData));
        InOrder inOrder = inOrder(
            courtServiceValidationTask,
            migrateCaseToPersonalServiceTask
        );
        inOrder.verify(courtServiceValidationTask).execute(context, caseData);
        inOrder.verify(migrateCaseToPersonalServiceTask).execute(context, caseData);
    }

    @Test
    public void testRunExecutesExpectedTasksInOrderForCaseStateAosAwaiting() throws WorkflowException, TaskException {
        caseData = Collections.singletonMap("SolServiceMethod", COURT_SERVICE_VALUE);
        setupTest(AOS_AWAITING, caseData);

        Map<String, Object> response = migrateToPersonalServicePackWorkflow.run(request, TEST_TOKEN);

        //then
        assertThat(response, is(caseData));
        InOrder inOrder = inOrder(
            courtServiceValidationTask,
            migrateCaseToPersonalServiceTask
        );
        inOrder.verify(courtServiceValidationTask).execute(context, caseData);
        inOrder.verify(migrateCaseToPersonalServiceTask).execute(context, caseData);
    }
}
