package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveMiniPetitionDraftDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseDataTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorSubmissionWorkflowTest {

    @Mock
    private ValidateSolicitorCaseDataTask validateSolicitorCaseDataTask;

    @Mock
    private ProcessPbaPaymentTask processPbaPaymentTask;

    @Mock
    private RemoveMiniPetitionDraftDocumentsTask removeMiniPetitionDraftDocumentsTask;

    @Mock
    private SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    @InjectMocks
    private SolicitorSubmissionWorkflow solicitorSubmissionWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        caseData = Collections.emptyMap();
        ccdCallbackRequestRequest = CcdCallbackRequest.builder()
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .caseDetails(CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(caseData)
                .build())
            .build();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        mockTasksExecution(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask
        );

        assertThat(solicitorSubmissionWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask
        );
    }
}
