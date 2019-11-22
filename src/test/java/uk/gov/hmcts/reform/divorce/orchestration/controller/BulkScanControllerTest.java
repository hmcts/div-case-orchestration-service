package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanValidationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.ERRORS;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanControllerTest {

    private static final String TEST_FORM_TYPE = "testFormType";

    @Mock
    private BulkScanValidationService bulkScanValidationService;

    @InjectMocks
    private BulkScanController bulkScanController;

    @Test
    public void shouldReturnValidatorResults() throws UnsupportedFormTypeException {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField("testName", "testValue"));
        when(bulkScanValidationService.validateBulkScanForm(eq(TEST_FORM_TYPE), eq(testOcrDataFields))).thenReturn(OcrValidationResult.builder()
            .addError("this is an error")
            .addWarning("this is a warning")
            .build()
        );

        ResponseEntity<OcrValidationResponse> response = bulkScanController.validateOcrData(null, TEST_FORM_TYPE, new OcrDataValidationRequest(
            testOcrDataFields
        ));

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody.getErrors(), hasItem("this is an error"));
        assertThat(responseBody.getWarnings(), hasItem("this is a warning"));
        assertThat(responseBody.getStatus(), is(ERRORS));
        verify(bulkScanValidationService).validateBulkScanForm(eq(TEST_FORM_TYPE), eq(testOcrDataFields));
    }

    @Test
    public void shouldReturnResourceNotFoundForUnsupportedFormType() throws UnsupportedFormTypeException {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField("testName", "testValue"));
        String unsupportedFormType = "unsupportedFormType";
        when(bulkScanValidationService.validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields)))
            .thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity<OcrValidationResponse> response = bulkScanController.validateOcrData(null, unsupportedFormType, new OcrDataValidationRequest(
            testOcrDataFields
        ));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody, is(nullValue()));
        verify(bulkScanValidationService).validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields));
    }
}