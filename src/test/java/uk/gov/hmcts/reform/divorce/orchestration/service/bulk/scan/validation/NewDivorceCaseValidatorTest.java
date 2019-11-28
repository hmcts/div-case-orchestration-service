package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.WARNINGS;

public class NewDivorceCaseValidatorTest {

    private final NewDivorceCaseValidator classUnderTest = new NewDivorceCaseValidator();
    private final OcrDataField validPetitionerPostcode = new OcrDataField("D8PetitionerPostCode", "HD7 5UZ");

    private final List<OcrDataField> listOfAllMandatoryFields = asList(
        new OcrDataField("D8PetitionerFirstName", "Peter"),
        new OcrDataField("D8PetitionerLastName", "Griffin"),
        new OcrDataField("D8LegalProcess", "Dissolution"),
        new OcrDataField("D8PaymentMethod", "Cheque"),
        new OcrDataField("D8ScreenHasMarriageCert", "True"),
        new OcrDataField("D8RespondentFirstName", "Louis"),
        new OcrDataField("D8RespondentLastName", "Griffin"),
        new OcrDataField("D8PetitionerNameChangedHow", "Yes"),
        new OcrDataField("D8PetitionerContactDetailsConfidential", "No"),
        new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
        new OcrDataField("D8MarriageRespondentName", "Louis Griffin"),
        new OcrDataField("D8ReasonForDivorceSeparationDate", "20/11/2008"),
        validPetitionerPostcode
    );

    @Test
    public void shouldPassValidationWhenMandatoryFieldsArePresent() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

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
            "Mandatory field \"D8RespondentLastName\" is missing",
            "Mandatory field \"D8MarriagePetitionerName\" is missing",
            "Mandatory field \"D8MarriageRespondentName\" is missing",
            "Mandatory field \"D8PetitionerNameChangedHow\" is missing",
            "Mandatory field \"D8PetitionerContactDetailsConfidential\" is missing",
            "Mandatory field \"D8PetitionerPostCode\" is missing"
        ));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerFirstName", "Kratos"),
            new OcrDataField("D8PetitionerLastName", ""),
            new OcrDataField("D8RespondentFirstName", ""),
            new OcrDataField("D8RespondentLastName", ""),
            new OcrDataField("D8MarriagePetitionerName", ""),
            new OcrDataField("D8MarriageRespondentName", ""),
            new OcrDataField("D8PetitionerNameChangedHow", ""),
            new OcrDataField("D8PetitionerPostCode", ""),
            new OcrDataField("D8PetitionerContactDetailsConfidential", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"D8PetitionerLastName\" is missing",
            "Mandatory field \"D8RespondentFirstName\" is missing",
            "Mandatory field \"D8RespondentLastName\" is missing",
            "Mandatory field \"D8MarriagePetitionerName\" is missing",
            "Mandatory field \"D8MarriageRespondentName\" is missing",
            "Mandatory field \"D8PetitionerNameChangedHow\" is missing",
            "Mandatory field \"D8PetitionerContactDetailsConfidential\" is missing",
            "Mandatory field \"D8PetitionerPostCode\" is missing"
        ));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8LegalProcess", "Bankruptcy"),
            new OcrDataField("D8ScreenHasMarriageCert", "Que?"),
            new OcrDataField("D8CertificateInEnglish", "What?"),
            new OcrDataField("D8PaymentMethod", "Bitcoin"),
            new OcrDataField("D8PetitionerNameChangedHow", "Si"),
            new OcrDataField("D8PetitionerPhoneNumber", "01213344"),
            new OcrDataField("D8PetitionerEmail", "aa@a"),
            new OcrDataField("D8PetitionerContactDetailsConfidential", "check"),
            new OcrDataField("D8PetitionerPostCode", "SW15 5PUX"),
            new OcrDataField("D8ReasonForDivorceSeparationDate", "this should be a date")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8LegalProcess must be \"Divorce\", \"Dissolution\" or \"Judicial (separation)\"",
            "D8ScreenHasMarriageCert must be \"True\"",
            "D8CertificateInEnglish must be \"True\" or left blank",
            "D8PetitionerPhoneNumber is not in a valid format",
            "D8PetitionerNameChangedHow must be \"Yes\" or \"No\"",
            "D8PetitionerEmail is not in a valid format",
            "D8PetitionerPostCode is usually 6 or 7 characters long",
            "D8PetitionerContactDetailsConfidential must be \"Yes\" or \"No\"",
            "D8CertificateInEnglish must be \"True\" or left blank",
            "D8ReasonForDivorceSeparationDate must be a valid date",
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
            new OcrDataField("D8RespondentLastName", "Griffin"),
            new OcrDataField("D8PetitionerNameChangedHow", "Yes"),
            new OcrDataField("D8PetitionerContactDetailsConfidential", "No"),
            new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
            new OcrDataField("D8MarriageRespondentName", "Louis Griffin"),
            validPetitionerPostcode
        ));

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfUsingInvalidHelpWithFeesNumberAndNoOtherPaymentMethod() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerFirstName", "Peter"),
            new OcrDataField("D8PetitionerLastName", "Griffin"),
            new OcrDataField("D8LegalProcess", "Dissolution"),
            new OcrDataField("D8HelpWithFeesReferenceNumber", "ABCDEF"),
            new OcrDataField("D8ScreenHasMarriageCert", "True"),
            new OcrDataField("D8RespondentFirstName", "Louis"),
            new OcrDataField("D8RespondentLastName", "Griffin"),
            new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
            new OcrDataField("D8MarriageRespondentName", "Louis Griffin")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8HelpWithFeesReferenceNumber is usually 6 digits"
        ));
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
            new OcrDataField("D8RespondentLastName", "Griffin"),
            new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
            new OcrDataField("D8MarriageRespondentName", "Louis Griffin")
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
            new OcrDataField("D8RespondentLastName", "Griffin"),
            new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
            new OcrDataField("D8MarriageRespondentName", "Louis Griffin")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value"
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassPetitionerPhoneNumberMatchingCustomValidationRules() {
        String[] validPhoneNumbers = {"07231334455", "+44 20 8356 3333", "01213334444"};
        for (String validPhoneNumber : validPhoneNumbers) {
            new ArrayList<>(listOfAllMandatoryFields).add(
                new OcrDataField("D8PetitionerPhoneNumber", validPhoneNumber)
            );

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), not(hasItems(
                "D8PetitionerPhoneNumber is not in a valid format"
            )));
        }
    }

    @Test
    public void shouldPassPetitionerEmailsMatchingCustomValidationRules() {
        String[] validEmailAddresses = {"aaa@gmail.com", "john.doe@mail.br", "akjl2489.rq23@a.co.uk"};
        for (String validEmailAddress : validEmailAddresses) {
            new ArrayList<>(listOfAllMandatoryFields).add(
                new OcrDataField("D8PetitionerEmail", validEmailAddress)
            );

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldPassPostcodeMatchingCustomValidationRules() {
        String[] validPostcodes = {"SW15 5PU", "M1 1AA", "B151TT", "SE279TU", "L1 0AP"};
        for (String validPostcode : validPostcodes) {
            Set<OcrDataField> ocrDataFieldSet = new HashSet<>(listOfAllMandatoryFields);
            ocrDataFieldSet.remove(validPetitionerPostcode);
            ocrDataFieldSet.add(new OcrDataField("D8PetitionerPostCode", validPostcode));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(new ArrayList<>(ocrDataFieldSet));

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldFailValidationWithWrongLeapYearDate() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8ReasonForDivorceSeparationDate", "29/02/2019")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8ReasonForDivorceSeparationDate must be a valid date"
        ));
    }

}