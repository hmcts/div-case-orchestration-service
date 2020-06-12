package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetCoRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class IssueEventWorkflowTest {

    @Mock
    private SetIssueDate setIssueDate;

    @Mock
    private ValidateCaseDataTask validateCaseDataTask;

    @Mock
    private PetitionGenerator petitionGenerator;

    @Mock
    private RespondentLetterGenerator respondentLetterGenerator;

    @Mock
    private CoRespondentLetterGenerator coRespondentLetterGenerator;

    @Mock
    private RespondentPinGenerator respondentPinGenerator;

    @Mock
    private CoRespondentPinGenerator coRespondentPinGenerator;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    private GetPetitionIssueFee getPetitionIssueFee;

    @Mock
    private ResetRespondentLinkingFields resetRespondentLinkingFields;

    @Mock
    private ResetCoRespondentLinkingFields resetCoRespondentLinkingFields;

    @Mock
    private CaseDataUtils caseDataUtils;

    @InjectMocks
    private IssueEventWorkflow issueEventWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        payload = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
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
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void givenCaseIsAdulteryWithNamedCoRespondent_AndRespondentLetterCanBeGenerated_whenRun_thenProceedAsExpected() throws WorkflowException {
        payload.put(D_8_DIVORCE_UNIT, CourtEnum.SERVICE_CENTER.getId());

        //Given
        when(validateCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(getPetitionIssueFee.execute(context, payload)).thenReturn(payload);
        when(coRespondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(coRespondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));
    }

    @Test
    public void givenGenerateAosInvitationIsTrueAndIsNottinghamDivorceUnit_whenRun_thenProceedAsExpected() throws WorkflowException {
        payload.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());

        //Given
        when(validateCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

    @Test
    public void givenCaseIsNotAdulteryWithNamedCoRespondent_AndRespondentLetterCanBeGenerated_whenRun_thenCoRespondentLetterIsNotGenerated() throws WorkflowException {
        payload.put(D_8_DIVORCE_UNIT, CourtEnum.SERVICE_CENTER.getId());

        //Given
        when(validateCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

    @Test
    public void givenGenerateAosInvitationIsTrueAndIsNotServiceCentre_whenRun_thenProceedAsExpected() throws WorkflowException {
        //Given
        when(validateCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(respondentPinGenerator);
        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(respondentLetterGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

    @Test
    public void givenGenerateAosInvitationIsFalse_whenRun_thenProceedAsExpected() throws WorkflowException {
        //Given
        when(validateCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, false);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(respondentPinGenerator);
        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(respondentLetterGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

}