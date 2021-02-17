package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute.DaRequestedPetitionerSolicitorEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class NotifyRespondentOfDARequestedWorkflowTest {

    @Mock
    private SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;

    @Mock
    private DaRequestedPetitionerSolicitorEmailTask daRequestedPetitionerSolicitorEmailTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;

    private final Map<String, Object> payload = new HashMap<>();

    @Test
    public void callsAllTasksWhenRespondentJourneyIsSwitchedOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build())
            .build();

        mockTasksExecution(
            payload,
            sendDaRequestedNotifyRespondentEmailTask,
            daRequestedPetitionerSolicitorEmailTask
        );

        assertThat(notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest), is(payload));
        verifyTasksCalledInOrder(
            payload,
            sendDaRequestedNotifyRespondentEmailTask,
            daRequestedPetitionerSolicitorEmailTask
        );
    }

    @Test
    public void callsOnlyOneTaskWhenRespondentJourneyIsSwitchedOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build())
            .build();

        mockTasksExecution(
            payload,
            sendDaRequestedNotifyRespondentEmailTask
        );

        assertThat(notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest), is(payload));
        verifyTasksCalledInOrder(
            payload,
            sendDaRequestedNotifyRespondentEmailTask
        );
        verifyTaskWasNeverCalled(daRequestedPetitionerSolicitorEmailTask);
    }
}
