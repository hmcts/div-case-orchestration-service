package uk.gov.hmcts.reform.divorce.orchestration.controller.helper;

import feign.FeignException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerUTest {
    private final GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    public void whenHandleBadRequestException_thenReturnUnderLyingError() {
        final int statusCode = HttpStatus.BAD_REQUEST.value();
        final String errorMessage = "some error message";

        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(feignException);

        assertEquals(statusCode, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }

}
