package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyPetitionerSolicitorDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraftForRefusalFromCaseIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SolicitorSubmitCaseToCCDTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FOR_REFUSAL_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewAmendedCaseAndSubmitToCCDWorkflowTest {


    @Mock
    private CreateAmendPetitionDraftForRefusalFromCaseIdTask createAmendPetitionDraftForRefusalFromCaseIdTask;

    @Mock
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Mock
    private CopyPetitionerSolicitorDetailsTask copyPetitionerSolicitorDetailsTask;

    @Mock
    private ValidateCaseDataTask validateCaseDataTask;

    @Mock
    private SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCDTask;

    @InjectMocks
    private CreateNewAmendedCaseAndSubmitToCCDWorkflow createNewAmendedCaseAndSubmitToCCDWorkflow;

    private CaseDetails caseDetails;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();

        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(testData)
            .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CCD_CASE_DATA, testData);
        context.setTransientObject(CASE_EVENT_ID_JSON_KEY, AMEND_PETITION_FOR_REFUSAL_EVENT);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws WorkflowException {
        Map<String, Object> newDivorceCaseData = ImmutableMap.of(PREVIOUS_CASE_ID_JSON_KEY, TEST_CASE_ID);
        Map<String, Object> newCCDCaseData = ImmutableMap.of(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        when(createAmendPetitionDraftForRefusalFromCaseIdTask.execute(context, testData)).thenReturn(newDivorceCaseData);
        when(formatDivorceSessionToCaseData.execute(context, newDivorceCaseData)).thenReturn(newCCDCaseData);
        when(copyPetitionerSolicitorDetailsTask.execute(context, newCCDCaseData)).thenReturn(newCCDCaseData);
        when(validateCaseDataTask.execute(context, newCCDCaseData)).thenReturn(newCCDCaseData);
        when(solicitorSubmitCaseToCCDTask.execute(context, newCCDCaseData)).thenReturn(newCCDCaseData);

        assertEquals(testData, createNewAmendedCaseAndSubmitToCCDWorkflow.run(caseDetails, AUTH_TOKEN));

        InOrder inOrder =
            inOrder(createAmendPetitionDraftForRefusalFromCaseIdTask, formatDivorceSessionToCaseData,
                copyPetitionerSolicitorDetailsTask, validateCaseDataTask, solicitorSubmitCaseToCCDTask);

        inOrder.verify(createAmendPetitionDraftForRefusalFromCaseIdTask).execute(context, testData);
        inOrder.verify(formatDivorceSessionToCaseData).execute(context, newDivorceCaseData);
        inOrder.verify(copyPetitionerSolicitorDetailsTask).execute(context, newCCDCaseData);
        inOrder.verify(validateCaseDataTask).execute(context, newCCDCaseData);
        inOrder.verify(solicitorSubmitCaseToCCDTask).execute(context, newCCDCaseData);
    }

}