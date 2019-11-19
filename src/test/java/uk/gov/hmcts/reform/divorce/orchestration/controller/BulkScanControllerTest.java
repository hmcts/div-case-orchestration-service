package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Before;
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.BulkScanFormValidator;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.BulkScanFormValidatorFactory;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanControllerTest {

    private static final String TEST_FORM_TYPE = "testFormType";

    @Mock
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    @InjectMocks
    private BulkScanController bulkScanController;

    private BulkScanFormValidator bulkScanFormValidator;

    @Before
    public void setUp() {
        bulkScanFormValidator = mock(BulkScanFormValidator.class);
    }

    @Test
    public void shouldReturnValidatorResults() {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField("testName", "testValue"));
        when(bulkScanFormValidator.validateBulkScanForm(eq(testOcrDataFields))).thenReturn(new OcrValidationResult(
            singletonList("this is a warning"),
            singletonList("this is an error"))
        );
        when(bulkScanFormValidatorFactory.getValidator(eq(TEST_FORM_TYPE))).thenReturn(bulkScanFormValidator);

        ResponseEntity<OcrValidationResponse> response = bulkScanController.validateOcrData(null, TEST_FORM_TYPE, new OcrDataValidationRequest(
            testOcrDataFields
        ));

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody.getErrors(), hasItem("this is an error"));
        assertThat(responseBody.getWarnings(), hasItem("this is a warning"));
        assertThat(responseBody.getStatus(), is(ValidationStatus.ERRORS));
        verify(bulkScanFormValidatorFactory).getValidator(eq(TEST_FORM_TYPE));
        verify(bulkScanFormValidator).validateBulkScanForm(eq(testOcrDataFields));
    }
}