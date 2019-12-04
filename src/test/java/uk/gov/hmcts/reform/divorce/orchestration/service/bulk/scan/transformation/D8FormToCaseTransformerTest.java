package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.D8FormToCaseTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

public class D8FormToCaseTransformerTest {

    private final D8FormToCaseTransformer classUnderTest = new D8FormToCaseTransformer();

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
    public void shouldReplaceCcdFieldForD8PaymentMethodIfPaymentMethodIsDebitCreditCard() {
        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(new OcrDataField("D8PaymentMethod", "Debit/Credit Card")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData.get("D8PaymentMethod"), is("Card"));
    }

    @Test
    public void shouldNotReplaceCcdFieldForD8PaymentMethodIfMethodIsCheque() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(new OcrDataField("D8PaymentMethod", "Cheque")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData.get("D8PaymentMethod"), is("Cheque"));
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

    @Test
    public void verifyPetitionerHomeAddressStreetIsCorrectlyAddedToPetitionerHomeAddress() {
        assertIsCorrectlyAddedToParentField(
                "D8PetitionerHomeAddress",
                "AddressLine1",
                "19 West Park Road",
                "D8PetitionerHomeAddressStreet"
        );
    }

    @Test
    public void verifyPetitionerPostcodeIsCorrectlyAddedToPetitionerHomeAddress() {
        ExceptionRecord incomingExceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField("D8PetitionerPostCode", "SE1 2PT")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);
        Map petitionerHomeAddress = (Map) transformedCaseData.get("D8PetitionerHomeAddress");

        assertThat(petitionerHomeAddress.get("PostCode"), is("SE1 2PT"));
    }

    @Test
    public void verifyPetitionerSolicitorAddressPostCodeIsCorrectlyAddedToPetitionerSolicitorAddress() {
        ExceptionRecord incomingExceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField("PetitionerSolicitorAddressPostCode", "SE1 2PT")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);
        Map petitionerHomeAddress = (Map) transformedCaseData.get("PetitionerSolicitorAddress");

        assertThat(petitionerHomeAddress.get("PostCode"), is("SE1 2PT"));
    }

    @Test
    public void verifyD8PetitionerCorrespondencePostcodeIsCorrectlyAddedToD8PetitionerCorrespondenceAddress() {
        ExceptionRecord incomingExceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField("D8PetitionerCorrespondencePostcode", "SE1 2PT")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);
        Map petitionerHomeAddress = (Map) transformedCaseData.get("D8PetitionerCorrespondenceAddress");

        assertThat(petitionerHomeAddress.get("PostCode"), is("SE1 2PT"));
    }

    private void assertIsCorrectlyAddedToParentField(String targetParentField, String targetChildField, String testValue, String sourceField) {
        ExceptionRecord incomingExceptionRecord =
                createExceptionRecord(singletonList(new OcrDataField(sourceField, testValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);
        Map parentField = (Map) transformedCaseData.get(targetParentField);

        assertThat(parentField.get(targetChildField), is(testValue));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }

}