package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.AosPack2FormToCaseTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

public class AosPack2FormToCaseTransformerTest {

    private final AosPack2FormToCaseTransformer classUnderTest = new AosPack2FormToCaseTransformer();

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(new OcrDataField("D8ReasonForDivorceSeparationDate", "20/11/2008")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8ReasonForDivorceSeperationDay", "20"),
            hasEntry("D8ReasonForDivorceSeperationMonth", "11"),
            hasEntry("D8ReasonForDivorceSeperationYear", "2008"),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(new OcrDataField("UnexpectedName", "UnexpectedValue")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    private void assertIsReplacedBy(String expectedValue, String inputValue, String field) {
        ExceptionRecord exceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField(field, inputValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData.get(field), is(expectedValue));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }

}