package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.D8FormToCaseTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
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
        assertIsReplacedBy("Card", "Debit/Credit Card", "D8PaymentMethod");
    }

    @Test
    public void shouldNotReplaceCcdFieldForD8PaymentMethodIfMethodIsCheque() {
        assertIsReplacedBy("Cheque", "Cheque", "D8PaymentMethod");
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
    public void shouldReplaceCcdFieldForD8FinancialOrderForIfIsForMyselfMyChildrenOrBoth() {
        assertIsReplacedBy("petitioner", "myself", "D8FinancialOrderFor");
        assertIsReplacedBy("children", "my children", "D8FinancialOrderFor");
        assertIsReplacedBy("petitioner, children", "myself, my children", "D8FinancialOrderFor");
    }


    @Test
    public void verifyPetitionerHomeAddressCountyIsCorrectlyAddedToPetitionerHomeAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerHomeAddress",
            "County",
            "West Midlands",
            "D8PetitionerHomeAddressCounty"
        );
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
    public void verifyPetitionerHomeAddressTownIsCorrectlyAddedToPetitionerHomeAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerHomeAddress",
            "PostTown",
            "Smethwick",
            "D8PetitionerHomeAddressTown"
        );
    }

    @Test
    public void verifyPetitionerPostcodeIsCorrectlyAddedToPetitionerHomeAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerHomeAddress",
            "PostCode",
            "SE1 2PT",
            "D8PetitionerPostCode"
        );
    }

    @Test
    public void verifyPetitionerSolicitorAddressCountyIsCorrectlyAddedToPetitionerSolicitorAddress() {
        assertIsCorrectlyAddedToParentField(
            "PetitionerSolicitorAddress",
            "County",
            "East Midlands",
            "PetitionerSolicitorAddressCounty"
        );
    }

    @Test
    public void verifyPetitionerSolicitorAddressStreetIsCorrectlyAddedToPetitionerSolicitorAddress() {
        assertIsCorrectlyAddedToParentField(
            "PetitionerSolicitorAddress",
            "AddressLine1",
            "20 solicitors road",
            "PetitionerSolicitorAddressStreet"
        );
    }

    @Test
    public void verifyPetitionerSolicitorAddressTownIsCorrectlyAddedToPetitionerSolicitorAddress() {
        assertIsCorrectlyAddedToParentField(
            "PetitionerSolicitorAddress",
            "PostTown",
            "Soltown",
            "PetitionerSolicitorAddressTown"
        );
    }

    @Test
    public void verifyPetitionerSolicitorAddressPostCodeIsCorrectlyAddedToPetitionerSolicitorAddress() {
        assertIsCorrectlyAddedToParentField(
            "PetitionerSolicitorAddress",
            "PostCode",
            "SE1 2PT",
            "PetitionerSolicitorAddressPostCode"
        );
    }

    @Test
    public void verifyD8PetitionerCorrespondenceAddressCountyIsCorrectlyAddedToD8PetitionerCorrespondenceAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerCorrespondenceAddress",
            "County",
            "South Midlands",
            "D8PetitionerCorrespondenceAddressCounty"
        );
    }

    @Test
    public void verifyD8PetitionerCorrespondenceAddressStreetIsCorrectlyAddedToD8PetitionerCorrespondenceAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerCorrespondenceAddress",
            "AddressLine1",
            "20 correspondence road",
            "D8PetitionerCorrespondenceAddressStreet"
        );
    }

    @Test
    public void verifyD8PetitionerCorrespondenceAddressTownIsCorrectlyAddedToD8PetitionerCorrespondenceAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerCorrespondenceAddress",
            "PostTown",
            "Correspondencetown",
            "D8PetitionerCorrespondenceAddressTown"
        );
    }

    @Test
    public void verifyD8PetitionerCorrespondencePostcodeIsCorrectlyAddedToD8PetitionerCorrespondenceAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8PetitionerCorrespondenceAddress",
            "PostCode",
            "SE12BP",
            "D8PetitionerCorrespondencePostcode"
        );
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyAddressStreetIsCorrectlyAddedToD8ReasonForDivorceAdultery3rdAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8ReasonForDivorceAdultery3rdAddress",
            "AddressLine1",
            "third party street",
            "D8ReasonForDivorceAdultery3rdPartyAddressStreet"
        );
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyTownIsCorrectlyAddedToD8ReasonForDivorceAdultery3rdAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8ReasonForDivorceAdultery3rdAddress",
            "PostTown",
            "Thirdtown",
            "D8ReasonForDivorceAdultery3rdPartyTown"
        );
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyCountyIsCorrectlyAddedToD8ReasonForDivorceAdultery3rdAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8ReasonForDivorceAdultery3rdAddress",
            "County",
            "North Midlands",
            "D8ReasonForDivorceAdultery3rdPartyCounty"
        );
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyPostCodeIsCorrectlyAddedToD8ReasonForDivorceAdultery3rdAddress() {
        assertIsCorrectlyAddedToParentField(
            "D8ReasonForDivorceAdultery3rdAddress",
            "PostCode",
            "SE3 5PP",
            "D8ReasonForDivorceAdultery3rdPartyPostCode"
        );
    }

    private void assertIsCorrectlyAddedToParentField(String targetParentField, String targetChildField, String testValue, String sourceField) {
        ExceptionRecord incomingExceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField(sourceField, testValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);
        Map parentField = (Map) transformedCaseData.get(targetParentField);

        assertThat(parentField.get(targetChildField), is(testValue));
    }

    private void assertIsReplacedBy(String expectedValue, String inputValue, String field) {
        ExceptionRecord exceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField(field, inputValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData.get(field), is(expectedValue));
    }

    @Test
    public void verifyMarriageDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8MarriageDateDay", "06"),
            new OcrDataField("D8MarriageDateMonth", "5"),
            new OcrDataField("D8MarriageDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8MarriageDate", "2007-05-06"),
            not(hasKey("D8MarriageDateDay")),
            not(hasEntry("D8MarriageDate", "2007-5-6")),
            not(hasEntry("D8MarriageDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifyD8MentalSeparationDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8MentalSeparationDateDay", "06"),
            new OcrDataField("D8MentalSeparationDateMonth", "5"),
            new OcrDataField("D8MentalSeparationDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8MentalSeparationDate", "2007-05-06"),
            not(hasKey("D8MentalSeparationDay")),
            not(hasEntry("D8MentalSeparationDate", "2007-5-6")),
            not(hasEntry("D8MentalSeparationDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifyD8PhysicalSeparationDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8PhysicalSeparationDateDay", "06"),
            new OcrDataField("D8PhysicalSeparationDateMonth", "5"),
            new OcrDataField("D8PhysicalSeparationDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8PhysicalSeparationDate", "2007-05-06"),
            not(hasKey("D8PhysicalSeparationDateDay")),
            not(hasEntry("D8PhysicalSeparationDate", "2007-5-6")),
            not(hasEntry("D8PhysicalSeparationDate", "2007-06-05"))
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }

}