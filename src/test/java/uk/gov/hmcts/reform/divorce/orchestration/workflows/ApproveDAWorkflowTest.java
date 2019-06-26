package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

import java.util.HashMap;
import java.util.Map;
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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;

@RunWith(MockitoJUnitRunner.class)
public class ApproveDAWorkflowTest {

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private ApproveDAWorkflow approveDAWorkflow;

    @Test
    public void callsTheRequiredTasksInOrder() throws WorkflowException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "auth");
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE);
        context.setTransientObject(DOCUMENT_FILENAME, DECREE_ABSOLUTE_FILENAME);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(documentGenerationTask.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);

        final Map<String, Object> result = approveDAWorkflow.run(ccdCallbackRequest, "auth");

        assertThat(result, is(payload));

        final InOrder inOrder = inOrder(documentGenerationTask, caseFormatterAddDocuments);

        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(caseFormatterAddDocuments).execute(context, payload);
    }

}
