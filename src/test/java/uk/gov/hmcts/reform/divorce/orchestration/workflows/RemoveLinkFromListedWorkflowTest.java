package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveCertificateOfEntitlementDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveListingDataTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class RemoveLinkFromListedWorkflowTest {

    @Mock
    private RemoveListingDataTask removeListingDataTask;
    @Mock
    private RemoveCertificateOfEntitlementDocumentsTask removeCertificateOfEntitlementDocumentsTask;

    @InjectMocks
    private RemoveLinkFromListedWorkflow classToTest;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> testData = Collections.emptyMap();

        mockTasksExecution(
            testData,
            removeListingDataTask,
            removeCertificateOfEntitlementDocumentsTask
        );

        assertThat(classToTest.run(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(testData).build()), is(testData));

        verifyTasksCalledInOrder(
            testData,
            removeListingDataTask,
            removeCertificateOfEntitlementDocumentsTask
        );
    }
}
