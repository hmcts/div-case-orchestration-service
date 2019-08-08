package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class IssueAosPackOfflineWorkflowTest {

    private static final String AOS_INVITATION_LETTER = "FL-DIV-LET-ENG-00070.doc";
    private static final String AOS_INVITATION_LETTER_DOCUMENT_TYPE = "aosinvitationletter-offline-resp";
    private static final String AOS_INVITATION_LETTER_DOCUMENT_FILENAME = "aos-invitation-letter-offline-respondent";

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private IssueAosPackOfflineWorkflow issueAosPackOfflineWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void testTasksAreCalledWithTheCorrectParams() throws WorkflowException {
        String testAuthToken = "authToken";
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        when(documentGenerationTask.execute(any(), any())).thenReturn(singletonMap("returnedKey1", "returnedValue1"));
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(singletonMap("returnedKey2", "returnedValue2"));

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .build();

        Map<String, Object> returnedPayload = issueAosPackOfflineWorkflow.run(testAuthToken, caseDetails);
        assertThat(returnedPayload, hasEntry("returnedKey2", "returnedValue2"));

        verify(documentGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(payload));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(testAuthToken));
        assertThat(taskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));
        assertThat(taskContext.getTransientObject(DOCUMENT_TEMPLATE_ID), is(AOS_INVITATION_LETTER));
        assertThat(taskContext.getTransientObject(DOCUMENT_TYPE), is(AOS_INVITATION_LETTER_DOCUMENT_TYPE));
        assertThat(taskContext.getTransientObject(DOCUMENT_FILENAME), is(AOS_INVITATION_LETTER_DOCUMENT_FILENAME));

        verify(caseFormatterAddDocuments).execute(any(), argThat(new HamcrestArgumentMatcher<>(allOf(
            Matchers.<String, Object>hasEntry("returnedKey1", "returnedValue1")
        ))));
    }

}