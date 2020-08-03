package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaseOrchestrationServiceExceptionTest {

    @Test
    public void shouldProduceExceptionWithCaseId_IfCaseIdIsGiven() {
        CaseOrchestrationServiceException exc = new CaseOrchestrationServiceException(new WorkflowException("Error message"), "123");
        assertThat(exc.getIdentifiableMessage(), is("Case id [123]: Error message"));
    }


    @Test
    public void shouldProduceExceptionWithoutCaseId_IfCaseIdIsNotGiven() {
        CaseOrchestrationServiceException exc = new CaseOrchestrationServiceException(new WorkflowException("Error message"));
        assertThat(exc.getIdentifiableMessage(), is("Error message"));
    }

}