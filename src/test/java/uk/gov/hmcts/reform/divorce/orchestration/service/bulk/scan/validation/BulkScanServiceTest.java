package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.CaseDetails;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanServiceTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Mock
    private BulkScanFormTransformer bulkScanFormTransformer;

    @Mock
    private BulkScanFormValidator validator;

    @Mock
    private BulkScanFormValidatorFactory factory;

    @InjectMocks
    private BulkScanService bulkScanService;

    @Test
    public void shouldCallReturnedValidator() {
        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField("myName", "myValue"));
        when(factory.getValidator("testForm")).thenReturn(validator);
        when(validator.validateBulkScanForm(ocrDataFields)).thenReturn(resultFromValidator);

        OcrValidationResult returnedResult = bulkScanService.validateBulkScanForm("testForm", ocrDataFields);

        verify(validator).validateBulkScanForm(ocrDataFields);
        assertThat(returnedResult, equalTo(resultFromValidator));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField("myName", "myValue"));
        when(factory.getValidator("unsupportedFormType")).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.validateBulkScanForm("unsupportedFormType", ocrDataFields);
    }

    @Test
    public void shouldCallReturnedTransformer() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .formType("testFormType")
            .ocrDataFields(singletonList(new OcrDataField("myName", "myValue")))
            .build();

        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        when(factory.getValidator("testFormType")).thenReturn(validator);
        when(validator.validateBulkScanForm(any())).thenReturn(resultFromValidator);
        when(bulkScanFormTransformerFactory.getTransformer("testFormType")).thenReturn(bulkScanFormTransformer);
        when(bulkScanFormTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(singletonMap("testKey", "testValue"));

        Map<String, Object> returnedResult = bulkScanService.transformBulkScanForm(exceptionRecord);

        verify(bulkScanFormTransformerFactory).getTransformer("testFormType");
        verify(bulkScanFormTransformer).transformIntoCaseData(exceptionRecord);
        assertThat(returnedResult, hasEntry("testKey", "testValue"));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFormTransformerFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);

        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType("unsupportedFormType").build();
        when(factory.getValidator("unsupportedFormType")).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.transformBulkScanForm(exceptionRecord);
    }

    @Test
    public void shouldThrowUnsupportedFormTypeExceptionFromFormTransformerFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);

        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType("unsupportedFormType").build();
        when(factory.getValidator("unsupportedFormType")).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.transformExceptionRecordAndUpdateExistingCase(exceptionRecord, CaseDetails.builder().build());
    }

    @Test
    public void shouldCallReturnedValidationAndTransformer() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .formType("testFormType")
            .ocrDataFields(singletonList(new OcrDataField("myName", "myValue")))
            .build();

        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        when(factory.getValidator("testFormType")).thenReturn(validator);
        when(validator.validateBulkScanForm(any())).thenReturn(resultFromValidator);
        when(bulkScanFormTransformerFactory.getTransformer("testFormType")).thenReturn(bulkScanFormTransformer);
        when(bulkScanFormTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(singletonMap("testKey", "testValue"));

        bulkScanService.transformExceptionRecordAndUpdateExistingCase(exceptionRecord, CaseDetails.builder().caseData(new HashMap<>()).build());

        verify(bulkScanFormTransformerFactory).getTransformer("testFormType");
        verify(bulkScanFormTransformer).transformIntoCaseData(exceptionRecord);
        verify(factory).getValidator("testFormType");
        verify(validator).validateBulkScanForm(exceptionRecord.getOcrDataFields());
    }

}