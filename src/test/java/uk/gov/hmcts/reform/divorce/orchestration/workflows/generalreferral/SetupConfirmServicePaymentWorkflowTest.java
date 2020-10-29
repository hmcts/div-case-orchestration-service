package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.GeneralApplicationWithoutNoticeFeeLookupTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.GetBailiffApplicationFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.SetupConfirmServicePaymentWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class SetupConfirmServicePaymentWorkflowTest {

    @Mock
    private GeneralApplicationWithoutNoticeFeeLookupTask generalApplicationWithoutNoticeFeeTask;

    @Mock
    private GetBailiffApplicationFeeTask getBailiffApplicationFeeTask;

    @InjectMocks
    private SetupConfirmServicePaymentWorkflow setupConfirmServicePaymentWorkflow;

    @Test
    public void whenGeneralApplicationWithoutNoticeFee_thenProcessGeneralAppFeeAsExpected() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, generalApplicationWithoutNoticeFeeTask);

        Map<String, Object> returned = setupConfirmServicePaymentWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasCalled(caseData, generalApplicationWithoutNoticeFeeTask);
    }

    @Test
    public void whenBaliffServiceType_thenProcessBaliffFeeAsExpected() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.BAILIFF);
        mockTasksExecution(
            caseData,
            getBailiffApplicationFeeTask
        );

        Map<String, Object> returned = setupConfirmServicePaymentWorkflow.run(
            CaseDetails.builder()
                    .caseData(caseData)
                    .build()
        );
        assertThat(returned, is(caseData));
        verifyTaskWasCalled(
            caseData,
            getBailiffApplicationFeeTask
        );
    }
}