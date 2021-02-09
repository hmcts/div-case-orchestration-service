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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AllowShareACaseTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class AllowShareACaseWorkflowTest {

    @Mock
    private AllowShareACaseTask allowShareACaseTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private AllowShareACaseWorkflow classToTest;

    private final CcdCallbackRequest request = buildRequest();
    private final Map<String, Object> caseData = request.getCaseDetails().getCaseData();

    @Test
    public void whenShareACaseIsOffCallOnlyAddPetitionerSolicitorRoleTask() throws Exception {
        assertThat(classToTest.run(request.getCaseDetails(), AUTH_TOKEN), is(caseData));

        verifyTaskWasNeverCalled(allowShareACaseTask);
    }

    @Test
    public void whenShareACaseIsOnCallAllTasks() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE)).thenReturn(true);

        mockTasksExecution(
            caseData,
            allowShareACaseTask
        );

        assertThat(classToTest.run(request.getCaseDetails(), AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            allowShareACaseTask
        );
    }

    private CcdCallbackRequest buildRequest() {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .build();

        return CcdCallbackRequest.builder().caseDetails(caseDetails).build();
    }
}
