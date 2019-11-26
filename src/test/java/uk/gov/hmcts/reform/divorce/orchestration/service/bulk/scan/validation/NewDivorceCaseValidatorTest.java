package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.WARNINGS;

public class NewDivorceCaseValidatorTest {

    private final NewDivorceCaseValidator classUnderTest = new NewDivorceCaseValidator();

    @Test
    public void shouldPassValidationWhenMandatoryFieldsArePresent() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerFirstName", "Peter"),
            new OcrDataField("D8PetitionerLastName", "Griffin"),
            new OcrDataField("D8LegalProcess", "Dissolution"),
            new OcrDataField("D8PaymentMethod", "Cheque"),
            new OcrDataField("D8ScreenHasMarriageCert", "True"),
            new OcrDataField("D8RespondentFirstName", "Louis"),
            new OcrDataField("D8RespondentLastName", "Griffin")
        ));

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsAreMissing() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(emptyList());

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"D8PetitionerFirstName\" is missing",
            "Mandatory field \"D8PetitionerLastName\" is missing",
            "Mandatory field \"D8LegalProcess\" is missing",
            "Mandatory field \"D8ScreenHasMarriageCert\" is missing",
            "Mandatory field \"D8RespondentFirstName\" is missing",
            "Mandatory field \"D8RespondentLastName\" is missing"
        ));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerFirstName", "Kratos"),
            new OcrDataField("D8PetitionerLastName", ""),
            new OcrDataField("D8RespondentFirstName", ""),
            new OcrDataField("D8RespondentLastName", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"D8PetitionerLastName\" is missing",
            "Mandatory field \"D8RespondentFirstName\" is missing",
            "Mandatory field \"D8RespondentLastName\" is missing"
        ));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8LegalProcess", "Bankruptcy"),
            new OcrDataField("D8ScreenHasMarriageCert", "Que?"),
            new OcrDataField("D8CertificateInEnglish", "What?"),
            new OcrDataField("D8PaymentMethod", "Bitcoin")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8LegalProcess must be \"Divorce\", \"Dissolution\" or \"Judicial (separation)\"",
            "D8ScreenHasMarriageCert must be \"True\"",
            "D8CertificateInEnglish must be \"True\" or left blank",
            "D8PaymentMethod must be \"Cheque\", \"Debit/Credit Card\" or left blank"
        ));
    }

    @Test
    public void shouldPassIfUsingValidHelpWithFeesNumberAndNoOtherPaymentMethod() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8PetitionerFirstName", "Peter"),
                new OcrDataField("D8PetitionerLastName", "Griffin"),
                new OcrDataField("D8LegalProcess", "Dissolution"),
                new OcrDataField("D8HelpWithFeesReferenceNumber", "123456"),
                new OcrDataField("D8ScreenHasMarriageCert", "True"),
                new OcrDataField("D8RespondentFirstName", "Louis"),
                new OcrDataField("D8RespondentLastName", "Griffin")
        ));

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfUsingMultipleValidPaymentMethods() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8PetitionerFirstName", "Peter"),
                new OcrDataField("D8PetitionerLastName", "Griffin"),
                new OcrDataField("D8LegalProcess", "Dissolution"),
                new OcrDataField("D8PaymentMethod", "Cheque"),
                new OcrDataField("D8HelpWithFeesReferenceNumber", "123456"),
                new OcrDataField("D8ScreenHasMarriageCert", "True"),
                new OcrDataField("D8RespondentFirstName", "Louis"),
                new OcrDataField("D8RespondentLastName", "Griffin")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItems(
                "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated"
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfNoPaymentMethodsProvided() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8PetitionerFirstName", "Peter"),
                new OcrDataField("D8PetitionerLastName", "Griffin"),
                new OcrDataField("D8LegalProcess", "Dissolution"),
                new OcrDataField("D8ScreenHasMarriageCert", "True"),
                new OcrDataField("D8RespondentFirstName", "Louis"),
                new OcrDataField("D8RespondentLastName", "Griffin")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItems(
                "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value"
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }
}