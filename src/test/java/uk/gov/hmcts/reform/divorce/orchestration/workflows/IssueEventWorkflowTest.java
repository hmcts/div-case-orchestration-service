package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetCoRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;

@RunWith(MockitoJUnitRunner.class)
public class IssueEventWorkflowTest {

    private IssueEventWorkflow issueEventWorkflow;

    @Mock
    private SetIssueDate setIssueDate;

    @Mock
    private ValidateCaseData validateCaseData;

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
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private GetPetitionIssueFee getPetitionIssueFee;

    @Mock
    private ResetRespondentLinkingFields resetRespondentLinkingFields;

    @Mock
    private ResetCoRespondentLinkingFields resetCoRespondentLinkingFields;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        issueEventWorkflow =
                new IssueEventWorkflow(
                        validateCaseData,
                        setIssueDate,
                        petitionGenerator,
                        respondentPinGenerator,
                        coRespondentPinGenerator,
                        respondentLetterGenerator,
                        getPetitionIssueFee,
                        coRespondentLetterGenerator,
                        caseFormatterAddDocuments,
                        resetRespondentLinkingFields,
                        resetCoRespondentLinkingFields);

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
    public void givenGenerateAosInvitationIsTrueAndIsServiceCentre_whenRun_thenProceedAsExpected() throws WorkflowException {
        payload.put("D8DivorceUnit", "serviceCentre");

        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

    @Test
    public void givenCaseIsAdulteryWithNamedCoRespondentAndRespondentLetterCanBeGenerated_whenRun_thenProceedAsExpected() throws WorkflowException {
        payload.put(D_8_DIVORCE_UNIT, DIVORCE_UNIT_SERVICE_CENTRE);
        payload.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        payload.put(D_8_CO_RESPONDENT_NAMED, "YES");

        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(getPetitionIssueFee.execute(context, payload)).thenReturn(payload);
        when(coRespondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(coRespondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));
    }

    @Test
    public void givenCaseIsNotAdulteryAndRespondentLetterCanBeGenerated_whenRun_thenCoRespondentLetterIsNotGenerated() throws WorkflowException {
        payload.put(D_8_DIVORCE_UNIT, DIVORCE_UNIT_SERVICE_CENTRE);
        payload.put(D_8_REASON_FOR_DIVORCE, "foo");

        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = issueEventWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN, true);

        //Then
        assertThat(response, is(payload));

        verifyZeroInteractions(getPetitionIssueFee);
        verifyZeroInteractions(coRespondentPinGenerator);
        verifyZeroInteractions(coRespondentLetterGenerator);
    }

    @Test
    public void givenCaseIsAdulteryButCoRespondentNotNamedAndRespondentLetterCanBeGenerated_whenRun_thenCoRespondentLetterIsNotGenerated() throws WorkflowException {
        payload.put(D_8_DIVORCE_UNIT, DIVORCE_UNIT_SERVICE_CENTRE);
        payload.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        payload.put(D_8_CO_RESPONDENT_NAMED, "No");

        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);
        when(resetCoRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

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
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
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
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
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

    @After
    public void tearDown() {
        issueEventWorkflow = null;
    }
}
