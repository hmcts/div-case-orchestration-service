package uk.gov.hmcts.reform.divorce.orchestration.controller.helper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleBadRequestException(FeignException exception) {
        log.warn(exception.getMessage(), exception);

        return handleFeignException(exception);
    }

    @ExceptionHandler(WorkflowException.class)
    ResponseEntity<Object> handleWorkFlowException(WorkflowException exception) {
        log.warn(exception.getMessage(), exception);

        if (exception.getCause() != null) {
            if (exception.getCause() instanceof FeignException) {
                return handleFeignException((FeignException) exception.getCause());
            }

            if (exception.getCause() instanceof TaskException && exception.getCause().getCause() != null) {
                return handleTaskException((TaskException) exception.getCause());
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    private ResponseEntity<Object> handleTaskException(TaskException taskException) {
        if (taskException.getCause() instanceof FeignException) {
            return handleFeignException((FeignException) taskException.getCause());
        }

        if (taskException.getCause() instanceof AuthenticationError) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(taskException.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(taskException.getMessage());
    }

    private ResponseEntity<Object> handleFeignException(FeignException exception) {
        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }
}
