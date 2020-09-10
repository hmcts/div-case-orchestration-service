package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.CaseDetails;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.update.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.bsp.common.model.update.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.ERRORS;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanControllerTest {

    private static final String TEST_FORM_TYPE = "testFormType";
    private static final String TEST_SERVICE_TOKEN = "testServiceToken";

    @Mock
    private BulkScanService bulkScanService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BulkScanController bulkScanController;

    @Test
    public void shouldReturnValidatorResults() {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField("testName", "testValue"));
        when(bulkScanService.validateBulkScanForm(eq(TEST_FORM_TYPE), eq(testOcrDataFields))).thenReturn(OcrValidationResult.builder()
            .addError("this is an error")
            .addWarning("this is a warning")
            .build()
        );

        ResponseEntity<OcrValidationResponse> response = bulkScanController
            .validateOcrData(TEST_SERVICE_TOKEN, TEST_FORM_TYPE, new OcrDataValidationRequest(testOcrDataFields));

        assertThat(response.getStatusCode(), is(OK));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody.getErrors(), hasItem("this is an error"));
        assertThat(responseBody.getWarnings(), hasItem("this is a warning"));
        assertThat(responseBody.getStatus(), is(ERRORS));
        verify(bulkScanService).validateBulkScanForm(eq(TEST_FORM_TYPE), eq(testOcrDataFields));

        verify(authService).assertIsServiceAllowedToValidate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnResourceNotFoundForUnsupportedFormType_ForValidation() {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField("testName", "testValue"));
        String unsupportedFormType = "unsupportedFormType";
        when(bulkScanService.validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields)))
            .thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity<OcrValidationResponse> response = bulkScanController
            .validateOcrData(TEST_SERVICE_TOKEN, unsupportedFormType, new OcrDataValidationRequest(testOcrDataFields));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody, is(nullValue()));
        verify(bulkScanService).validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields));

        verify(authService).assertIsServiceAllowedToValidate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnTransformerServiceResults() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_FORM_TYPE).build();
        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenReturn(singletonMap("testKey", "testValue"));

        ResponseEntity<SuccessfulTransformationResponse> response =
            bulkScanController.transformExceptionRecordIntoCase(TEST_SERVICE_TOKEN, exceptionRecord);

        assertThat(response.getStatusCode(), is(OK));
        SuccessfulTransformationResponse transformationResponse = response.getBody();
        assertThat(transformationResponse.getWarnings(), is(emptyList()));
        CaseCreationDetails caseCreationDetails = transformationResponse.getCaseCreationDetails();
        assertThat(caseCreationDetails.getCaseTypeId(), is("DIVORCE"));
        assertThat(caseCreationDetails.getCaseData(), hasEntry("testKey", "testValue"));

        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnErrorForUnsupportedFormType_ForTransformation() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_FORM_TYPE).build();
        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity response = bulkScanController.transformExceptionRecordIntoCase(TEST_SERVICE_TOKEN, exceptionRecord);

        assertThat(response.getStatusCode(), is(UNPROCESSABLE_ENTITY));
        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnUpdatedCase() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_FORM_TYPE).build();
        CaseDetails existingCaseDetails = CaseDetails.builder().caseData(emptyMap()).build();
        CaseDetails updatedCaseDetails = CaseDetails.builder().caseData(singletonMap("testKey", "testValue")).build();

        when(bulkScanService.transformExceptionRecordAndUpdateExistingCase(exceptionRecord, existingCaseDetails))
            .thenReturn(updatedCaseDetails);

        ResponseEntity<SuccessfulUpdateResponse> response =
            bulkScanController.updateCase(
                TEST_SERVICE_TOKEN, new BulkScanCaseUpdateRequest(exceptionRecord, existingCaseDetails)
            );

        assertThat(response.getStatusCode(), is(OK));

        SuccessfulUpdateResponse updateResponse = response.getBody();
        CaseDetails returnedCaseDetails = updateResponse.getCaseDetails();

        assertThat(updateResponse.getWarnings(), is(emptyList()));
        assertThat(returnedCaseDetails.getCaseTypeId(), is("DIVORCE"));
        assertThat(returnedCaseDetails.getCaseData(), hasEntry("testKey", "testValue"));

        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnErrorForUnsupportedFormType_ForUpdate() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_FORM_TYPE).build();
        CaseDetails existingCaseDetails = CaseDetails.builder().caseData(emptyMap()).build();

        when(bulkScanService.transformExceptionRecordAndUpdateExistingCase(exceptionRecord, existingCaseDetails))
            .thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity response = bulkScanController.updateCase(
            TEST_SERVICE_TOKEN, new BulkScanCaseUpdateRequest(exceptionRecord, existingCaseDetails)
        );

        assertThat(response.getStatusCode(), is(UNPROCESSABLE_ENTITY));
        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }
}
