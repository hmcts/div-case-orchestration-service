package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanValidationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanValidationServiceTest {

    @Mock
    private BulkScanFormValidator validator;

    @Mock
    private BulkScanFormValidatorFactory factory;

    @InjectMocks
    private BulkScanValidationService bulkScanValidationService;

    @Test
    public void shouldCallReturnedValidator() throws UnsupportedFormTypeException {
        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField("myName", "myValue"));
        when(factory.getValidator("testForm")).thenReturn(validator);
        when(validator.validateBulkScanForm(ocrDataFields)).thenReturn(resultFromValidator);

        OcrValidationResult returnedResult = bulkScanValidationService.validateBulkScanForm("testForm", ocrDataFields);

        verify(validator).validateBulkScanForm(ocrDataFields);
        assertThat(returnedResult, equalTo(resultFromValidator));
    }

}