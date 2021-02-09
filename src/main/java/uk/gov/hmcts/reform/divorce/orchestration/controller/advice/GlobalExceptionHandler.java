package uk.gov.hmcts.reform.divorce.orchestration.controller.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.out.BspErrorResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @ExceptionHandler(InvalidDataException.class)
    ResponseEntity<Object> handleInvalidDataException(InvalidDataException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                BspErrorResponse.builder()
                    .errors(mergeLists(exception.getWarnings(), exception.getErrors()))
                    .build()
            );
    }

    private ResponseEntity<Object> handleTaskException(TaskException taskException) {
        ResponseEntity<Object> responseEntity;

        String exceptionMessage = taskException.getMessage();

        if (taskException.getCause() instanceof FeignException) {
            responseEntity = handleFeignException((FeignException) taskException.getCause());
        } else if (taskException.getCause() instanceof AuthenticationError) {
            responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionMessage);
        } else if (taskException.getCause() instanceof CaseNotFoundException) {
            responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionMessage);
        } else if (taskException.getCause() instanceof ValidationException) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        } else {
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionMessage);
        }

        return responseEntity;
    }

    private ResponseEntity<Object> handleFeignException(FeignException exception) {
        int status = exception.status();
        if (status == HttpStatus.MULTIPLE_CHOICES.value()) {
            return ResponseEntity.status(exception.status()).body(null);
        }
        return ResponseEntity.status(exception.status()).body(
            String.format("%s - %s", exception.getMessage(), exception.contentUTF8())
        );
    }

    private List<String> mergeLists(List<String> warn, List<String> errors) {
        warn = Optional.ofNullable(warn).orElse(new ArrayList<>());
        warn.addAll(errors);

        return warn;
    }
}
