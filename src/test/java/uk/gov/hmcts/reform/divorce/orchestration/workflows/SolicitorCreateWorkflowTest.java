package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AllowShareACaseTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetClaimCostsFromTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorCourtDetailsTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateWorkflowTest {

    @Mock
    AddMiniPetitionDraftTask addMiniPetitionDraftTask;

    @Mock
    AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask;

    @Mock
    SetClaimCostsFromTask setClaimCostsFromTask;

    @Mock
    AllowShareACaseTask allowShareACaseTask;

    @Mock
    FeatureToggleService featureToggleService;

    @InjectMocks
    SolicitorCreateWorkflow solicitorCreateWorkflow;

    @Test
    public void runShouldNotExecuteAllowShareACaseTaskWhenFeatureToggleOff() throws Exception {
        Map<String, Object> payload = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        mockTasksExecution(
            caseDetails.getCaseData(),
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );


        assertEquals(payload, solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN));

        verifyTasksCalledInOrder(
            payload,
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );
    }

    @Test
    public void runShouldExecuteAllowShareACaseTaskWhenFeatureToggleOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE)).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();

        mockTasksExecution(
            caseDetails.getCaseData(),
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            allowShareACaseTask
        );

        assertEquals(caseDetails.getCaseData(), solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN));

        verifyTasksCalledInOrder(
            caseDetails.getCaseData(),
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            allowShareACaseTask
        );
    }

    @Test
    public void runShouldSetClaimCostsFromWhenClaimCostsIsYesAndClaimCostsFromIsEmpty() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        mockTasksExecution(
            caseDetails.getCaseData(),
            setClaimCostsFromTask,
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );

        assertEquals(caseDetails.getCaseData(), solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN));

        verifyTasksCalledInOrder(
            caseDetails.getCaseData(),
            setClaimCostsFromTask,
            setSolicitorCourtDetailsTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );
    }
}
