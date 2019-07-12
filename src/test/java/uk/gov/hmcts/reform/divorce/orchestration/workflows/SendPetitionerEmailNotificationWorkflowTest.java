package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmail;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerEmailNotificationWorkflowTest {

    @Mock
    private SendPetitionerSubmissionNotificationEmail sendPetitionerSubmissionNotificationEmail;

    @Mock
    private SendPetitionerUpdateNotificationsEmail sendPetitionerUpdateNotificationsEmail;

    @InjectMocks
    private SendPetitionerSubmissionNotificationWorkflow sendPetitionerSubmissionNotificationWorkflow;

    @InjectMocks
    private SendPetitionerEmailNotificationWorkflow sendPetitionerEmailNotificationWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(testData)
            .build();
        ccdCallbackRequestRequest =
                CcdCallbackRequest.builder()
                        .eventId(TEST_EVENT_ID)
                        .token(TEST_TOKEN)
                        .caseDetails(
                            caseDetails
                        )
                        .build();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void genericEmailTaskShouldExecuteAndReturnPayload() throws Exception {
        context.setTransientObject(CASE_EVENT_ID_JSON_KEY, TEST_EVENT_ID);
        when(sendPetitionerUpdateNotificationsEmail.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, sendPetitionerEmailNotificationWorkflow.run(ccdCallbackRequestRequest));

        verify(sendPetitionerUpdateNotificationsEmail).execute(context, testData);
    }

    @Test
    public void submissionEmailTaskShouldExecuteAndReturnPayload() throws Exception {
        when(sendPetitionerSubmissionNotificationEmail.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequestRequest));

        verify(sendPetitionerSubmissionNotificationEmail).execute(context, testData);
    }
}
