package uk.gov.hmcts.reform.divorce.context;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleBadRequestException(FeignException exception) {
        log.error(String.format("HTTP request error: %s", exception.contentUTF8(), exception));
        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }
}
