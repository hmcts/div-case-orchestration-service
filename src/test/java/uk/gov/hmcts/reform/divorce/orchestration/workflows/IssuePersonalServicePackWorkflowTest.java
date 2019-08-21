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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendSolicitorPersonalServiceEmailTask;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID;

@RunWith(MockitoJUnitRunner.class)
public class IssuePersonalServicePackWorkflowTest {

    @Mock
    DocumentGenerationTask documentGenerationTask;

    @Mock
    CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    SendSolicitorPersonalServiceEmailTask sendSolicitorPersonalServiceEmailTask;

    @InjectMocks
    IssuePersonalServicePackWorkflow issuePersonalServicePackWorkflow;

    @Test
    public void testRunExecutesExpectedTasksInOrder() throws WorkflowException, TaskException {
        //given
        Map<String, Object> caseData = Collections.singletonMap("key", "value");
        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObjects(new HashMap<String, Object>() {
            {
                put(AUTH_TOKEN_JSON_KEY, TEST_TOKEN);
                put(CASE_ID_JSON_KEY, TEST_CASE_ID);
                put(CASE_DETAILS_JSON_KEY, caseDetails);
                put(DOCUMENT_TYPE, SOLICITOR_PERSONAL_SERVICE_LETTER_DOCUMENT_TYPE);
                put(DOCUMENT_TEMPLATE_ID, SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
                put(DOCUMENT_FILENAME, SOLICITOR_PERSONAL_SERVICE_LETTER_FILENAME);
            }
        });

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        //when
        when(documentGenerationTask.execute(context, caseData)).thenReturn(caseData);
        when(caseFormatterAddDocuments.execute(context, caseData)).thenReturn(caseData);
        when(sendSolicitorPersonalServiceEmailTask.execute(context, caseData)).thenReturn(caseData);
        Map<String, Object> response = issuePersonalServicePackWorkflow.run(request, TEST_TOKEN);

        //then
        assertThat(response, is(caseData));
        InOrder inOrder = inOrder(documentGenerationTask, caseFormatterAddDocuments, sendSolicitorPersonalServiceEmailTask);
        inOrder.verify(documentGenerationTask).execute(context, caseData);
        inOrder.verify(caseFormatterAddDocuments).execute(context, caseData);
        inOrder.verify(sendSolicitorPersonalServiceEmailTask).execute(context, caseData);
    }
}
