package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddPetitionerSolicitorRole;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class PetitionerSolicitorRoleWorkflowUTest {

    @Mock
    private AddPetitionerSolicitorRole addPetitionerSolicitorRole;

    @InjectMocks
    private PetitionerSolicitorRoleWorkflow classToTest;

    @Test
    public void givenCaseSubmitted_whenRunWorkflow_thenCorrectTasksAreCalled() throws TaskException, WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(ImmutableMap.of(
                    SOLICITOR_REFERENCE_JSON_KEY, TestConstants.TEST_SOLICITOR_REFERENCE,
                    PETITIONER_SOLICITOR_EMAIL, TestConstants.TEST_USER_LAST_NAME,
                    D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL))
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(addPetitionerSolicitorRole.execute(any(),
            eq(caseDetails.getCaseData()))).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        assertEquals(caseDetails.getCaseData(), response);
    }

}
