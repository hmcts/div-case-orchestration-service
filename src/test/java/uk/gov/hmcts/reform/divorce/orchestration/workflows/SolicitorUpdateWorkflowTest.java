package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyD8JurisdictionConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetNewLegalConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetPetitionerSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetRespondentSolicitorOrganisationPolicyReferenceTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorUpdateWorkflowTest {

    @Mock
    private AddMiniPetitionDraftTask addMiniPetitionDraftTask;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    private SetPetitionerSolicitorOrganisationPolicyReferenceTask setPetitionerSolicitorOrganisationPolicyReferenceTask;

    @Mock
    private SetRespondentSolicitorOrganisationPolicyReferenceTask setRespondentSolicitorOrganisationPolicyReferenceTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;

    @Mock
    private CopyD8JurisdictionConnectionPolicyTask copyD8JurisdictionConnectionPolicyTask;

    @InjectMocks
    private SolicitorUpdateWorkflow solicitorUpdateWorkflow;

    private Map<String, Object> caseData;
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseData = Collections.emptyMap();
        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        mockTasksExecution(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addNewDocumentsToCaseDataTask,
            addMiniPetitionDraftTask
        );

        executeWorkflow();

        verifyTasksCalledInOrder(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask

        );
    }

    @Test
    public void runShouldRunSetSolicitorOrganisationPolicyReferenceTaskWhenFeatureIsOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        mockTasksExecution(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            setPetitionerSolicitorOrganisationPolicyReferenceTask,
            setRespondentSolicitorOrganisationPolicyReferenceTask
        );

        executeWorkflow();

        verifyTasksCalledInOrder(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask,
            setPetitionerSolicitorOrganisationPolicyReferenceTask,
            setRespondentSolicitorOrganisationPolicyReferenceTask
        );
    }

    @Test
    public void runShouldNotRunSetSolicitorOrganisationPolicyReferenceTaskWhenFeatureIsOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        mockTasksExecution(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );

        executeWorkflow();

        verifyTasksCalledInOrder(
            caseData,
            setNewLegalConnectionPolicyTask,
            copyD8JurisdictionConnectionPolicyTask,
            addMiniPetitionDraftTask,
            addNewDocumentsToCaseDataTask
        );

        verifyTasksWereNeverCalled(setPetitionerSolicitorOrganisationPolicyReferenceTask);
        verifyTasksWereNeverCalled(setRespondentSolicitorOrganisationPolicyReferenceTask);
    }

    private void executeWorkflow() throws WorkflowException {
        Map<String, Object> resultCaseData = solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(caseData, is(resultCaseData));
    }
}
