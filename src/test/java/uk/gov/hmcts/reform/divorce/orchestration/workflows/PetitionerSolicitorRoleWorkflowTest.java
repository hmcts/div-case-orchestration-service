package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddPetitionerSolicitorRoleTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class PetitionerSolicitorRoleWorkflowTest {

    @Mock
    private AddPetitionerSolicitorRoleTask addPetitionerSolicitorRoleTask;

    @InjectMocks
    private PetitionerSolicitorRoleWorkflow classToTest;

    private final CcdCallbackRequest request = buildRequest();
    private final Map<String, Object> caseData = request.getCaseDetails().getCaseData();

    @Test
    public void givenCaseSubmitted_whenShareACaseIsOff_thenCallOnlyAddPetitionerSolicitorRoleTask() throws Exception {
        mockTasksExecution(
            caseData,
            addPetitionerSolicitorRoleTask
        );

        assertThat(classToTest.run(request, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            addPetitionerSolicitorRoleTask
        );
    }

    private CcdCallbackRequest buildRequest() {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(
                ImmutableMap.of(
                    SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE,
                    PETITIONER_SOLICITOR_EMAIL, TEST_USER_LAST_NAME,
                    D_8_PETITIONER_EMAIL, TEST_USER_EMAIL
                )
            ).build();

        return CcdCallbackRequest.builder().caseDetails(caseDetails).build();
    }
}
