package uk.gov.hmcts.reform.divorce.orchestration.controller.helper;

import feign.FeignException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;

public class GlobalExceptionHandlerUTest {
    private static  final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();

    private final GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    public void whenHandleBadRequestException_thenReturnUnderLyingError() {
        final FeignException feignException = getFeignException();

        ResponseEntity<Object> actual = classUnderTest.handleBadRequestException(feignException);

        assertEquals(STATUS_CODE, actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenNoCause_whenHandleWorkFlowException_thenReturnInternalServerError() {
        final WorkflowException workflowException = new WorkflowException(TEST_ERROR);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseInstanceOfFeignException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getFeignException();

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, feignException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(STATUS_CODE, actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseForMultiCases_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getMultiFeignException();

        final WorkflowException workflowException = new WorkflowException("", feignException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), actual.getStatusCodeValue());
        assertNull(actual.getBody());
    }

    @Test
    public void givenCauseInstanceOfTaskException_whenHandleWorkFlowException_thenReturnInternalServerError() {
        final TaskException taskException = new TaskException(TEST_ERROR);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsFeignException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getFeignException();

        final TaskException taskException = new TaskException(TEST_ERROR, feignException);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(STATUS_CODE, actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsNoFeignNoAuthException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final Exception exception = new Exception(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, exception);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR.value(), actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsAuthorizationError_whenHandleWorkFlowException_thenReturnUnAuthorized() {
        final AuthenticationError authenticationError = new AuthenticationError(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, authenticationError);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsCaseNotFoundException_whenHandleWorkFlowException_thenReturnNotFound() {
        final CaseNotFoundException caseNotFoundException = new CaseNotFoundException(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, caseNotFoundException);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = classUnderTest.handleWorkFlowException(workflowException);

        assertEquals(HttpStatus.NOT_FOUND.value(), actual.getStatusCodeValue());
        assertEquals(TEST_ERROR, actual.getBody());
    }


    private FeignException getFeignException() {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(GlobalExceptionHandlerUTest.STATUS_CODE);
        when(feignException.getMessage()).thenReturn(TEST_ERROR);
        return feignException;
    }

    private FeignException getMultiFeignException() {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(HttpStatus.MULTIPLE_CHOICES.value());
        when(feignException.getMessage()).thenReturn("");
        return feignException;
    }
}
