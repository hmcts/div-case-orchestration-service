package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;

import java.util.List;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;

@RunWith(MockitoJUnitRunner.class)
public class IssueAosPackOfflineWorkflowTest {

    private static final String RESPONDENT_AOS_INVITATION_LETTER = "FL-DIV-LET-ENG-00070.doc";
    private static final String RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE = "aosinvitationletter-offline-resp";
    private static final String RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME = "aos-invitation-letter-offline-respondent";

    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER = "FL-DIV-LET-ENG-00076.doc";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE = "aosinvitationletter-offline-co-resp";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME = "aos-invitation-letter-offline-co-respondent";

    @Mock
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private IssueAosPackOfflineWorkflow issueAosPackOfflineWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    private String testAuthToken = "authToken";
    private CaseDetails caseDetails;

    @Before
    public void setUp() throws TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        when(documentsGenerationTask.execute(any(), any())).thenReturn(singletonMap("returnedKey1", "returnedValue1"));
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(singletonMap("returnedKey2", "returnedValue2"));
        caseDetails = CaseDetails.builder().caseData(payload).build();
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent() throws WorkflowException, TaskException {
        Map<String, Object> returnedPayload = issueAosPackOfflineWorkflow.run(testAuthToken, caseDetails, DivorceParty.RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey2", "returnedValue2"));

        verify(documentsGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(caseDetails.getCaseData()));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(testAuthToken));
        assertThat(taskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));

        List<DocumentGenerationRequest> documentGenerationRequestList = taskContext.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);
        DocumentGenerationRequest firstDocumentGenerationRequest = documentGenerationRequestList.get(0);
        assertThat(firstDocumentGenerationRequest.getDocumentTemplateId(), is(RESPONDENT_AOS_INVITATION_LETTER));
        assertThat(firstDocumentGenerationRequest.getDocumentType(), is(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE));
        assertThat(firstDocumentGenerationRequest.getDocumentFileName(), is(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME));

        verify(caseFormatterAddDocuments).execute(any(), argThat(new HamcrestArgumentMatcher<>(allOf(
            Matchers.<String, Object>hasEntry("returnedKey1", "returnedValue1")
        ))));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> returnedPayload = issueAosPackOfflineWorkflow.run(testAuthToken, caseDetails, DivorceParty.CO_RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey2", "returnedValue2"));

        verify(documentsGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(caseDetails.getCaseData()));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(testAuthToken));
        assertThat(taskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));

        List<DocumentGenerationRequest> documentGenerationRequestList = taskContext.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);
        DocumentGenerationRequest firstDocumentGenerationRequest = documentGenerationRequestList.get(0);
        assertThat(firstDocumentGenerationRequest.getDocumentTemplateId(), is(CO_RESPONDENT_AOS_INVITATION_LETTER));
        assertThat(firstDocumentGenerationRequest.getDocumentType(), is(CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE));
        assertThat(firstDocumentGenerationRequest.getDocumentFileName(), is(CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME));

        verify(caseFormatterAddDocuments).execute(any(), argThat(new HamcrestArgumentMatcher<>(allOf(
            Matchers.<String, Object>hasEntry("returnedKey1", "returnedValue1")
        ))));
    }

}