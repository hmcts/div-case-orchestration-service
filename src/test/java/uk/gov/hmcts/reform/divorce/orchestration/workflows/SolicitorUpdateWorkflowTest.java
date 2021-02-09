package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorOrganisationPolicyDetailsTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorUpdateWorkflowTest {

    @Mock
    AddMiniPetitionDraftTask addMiniPetitionDraftTask;

    @Mock
    AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    SetSolicitorOrganisationPolicyDetailsTask setSolicitorOrganisationPolicyDetailsTask;

    @Mock
    FeatureToggleService featureToggleService;

    @InjectMocks
    SolicitorUpdateWorkflow solicitorUpdateWorkflow;

    private Map<String, Object> caseData;
    private TaskContext context;
    private CaseDetails caseDetails;


    @Before
    public void setup() {
        caseData = Collections.emptyMap();
        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        mockTasksExecution(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );

        Map<String, Object> resultCaseData = solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN);

        assertThat(caseData, is(resultCaseData));

        verifyTasksCalledInOrder(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );
    }

    @Test
    public void runShouldRunSetSolicitorOrganisationPolicyDetailsTaskWhenFeatureIsOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        mockTasksExecution(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            setSolicitorOrganisationPolicyDetailsTask
        );

        Map<String, Object> resultCaseData = solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(caseData, is(resultCaseData));

        verifyTasksCalledInOrder(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            setSolicitorOrganisationPolicyDetailsTask
        );
    }

    @Test
    public void runShouldNotRunSetSolicitorOrganisationPolicyDetailsTaskWhenFeatureIsOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        mockTasksExecution(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );

        Map<String, Object> resultCaseData = solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(caseData, is(resultCaseData));

        verifyTasksCalledInOrder(
            caseData,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );
    }
}
