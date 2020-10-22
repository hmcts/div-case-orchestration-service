package uk.gov.hmcts.reform.divorce.orchestration.controller.util;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

public class CallbackControllerTestUtils {

    public static void assertCaseOrchestrationServiceExceptionIsSetProperly(CaseOrchestrationServiceException exception) {
        assertThat(exception.getCause(), instanceOf(WorkflowException.class));
        Optional<String> caseId = exception.getCaseId();
        assertThat(caseId.isPresent(), is(true));
        assertThat(caseId.get(), is(TEST_CASE_ID));
    }
}
