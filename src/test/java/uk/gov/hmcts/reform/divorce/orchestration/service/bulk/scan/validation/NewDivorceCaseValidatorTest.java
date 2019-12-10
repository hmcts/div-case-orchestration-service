package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.WARNINGS;

public class NewDivorceCaseValidatorTest {

    private final NewDivorceCaseValidator classUnderTest = new NewDivorceCaseValidator();
    private List<OcrDataField> listOfAllMandatoryFields;
    private OcrDataField validD8paymentMethod = new OcrDataField("D8PaymentMethod", "Cheque");

    @Before
    public void setup() {
        List<OcrDataField> listOfAllMandatoryFieldsImmutable = asList(
            new OcrDataField("D8PetitionerFirstName", "Peter"),
            new OcrDataField("D8PetitionerLastName", "Griffin"),
            new OcrDataField("D8LegalProcess", "Dissolution"),
            validD8paymentMethod,
            new OcrDataField("D8ScreenHasMarriageCert", "True"),
            new OcrDataField("D8RespondentFirstName", "Louis"),
            new OcrDataField("D8RespondentLastName", "Griffin"),
            new OcrDataField("D8PetitionerNameChangedHow", "Yes"),
            new OcrDataField("D8PetitionerContactDetailsConfidential", "No"),
            new OcrDataField("D8MarriagePetitionerName", "Peter Griffin"),
            new OcrDataField("D8MarriageRespondentName", "Louis Griffin"),
            new OcrDataField("D8ReasonForDivorceSeparationDate", "20/11/2008"),
            new OcrDataField("D8PetitionerPostCode", "HD7 5UZ"),
            new OcrDataField("PetitionerSolicitor", "Yes"),
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "No"),
            new OcrDataField("D8PetitionerHomeAddressStreet", "19 West Park Road"),
            new OcrDataField("D8PetitionerHomeAddressTown", "Smethwick"),
            new OcrDataField("D8PetitionerHomeAddressCounty", "West Midlands")
        );

        listOfAllMandatoryFields = new ArrayList<>(listOfAllMandatoryFieldsImmutable);
    }

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
            "Mandatory field \"D8PetitionerPostCode\" is missing",
            "Mandatory field \"PetitionerSolicitor\" is missing",
            "Mandatory field \"D8PetitionerCorrespondenceUseHomeAddress\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressStreet\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressTown\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressCounty\" is missing"
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
            new OcrDataField("D8PetitionerContactDetailsConfidential", ""),
            new OcrDataField("PetitionerSolicitor", ""),
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", ""),
            new OcrDataField("D8PetitionerHomeAddressStreet", ""),
            new OcrDataField("D8PetitionerHomeAddressTown", ""),
            new OcrDataField("D8PetitionerHomeAddressCounty", "")
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
            "Mandatory field \"D8PetitionerPostCode\" is missing",
            "Mandatory field \"PetitionerSolicitor\" is missing",
            "Mandatory field \"D8PetitionerCorrespondenceUseHomeAddress\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressStreet\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressTown\" is missing",
            "Mandatory field \"D8PetitionerHomeAddressCounty\" is missing"
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
            new OcrDataField("D8RespondentPhoneNumber", "01213344"),
            new OcrDataField("D8ReasonForDivorceSeparationDate", "this should be a date"),
            new OcrDataField("PetitionerSolicitor", "I don't have one"),
            new OcrDataField("PetitionerSolicitorAddressPostCode", "m4 2a"),
            new OcrDataField("PetitionerSolicitorPhone", "07700900four"),
            new OcrDataField("PetitionerSolicitorEmail", "nece28ssito@no."),
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "Where"),
            new OcrDataField("D8PetitionerCorrespondencePostcode", "TR13 8BCD1")
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
            "D8PetitionerPhoneNumber is not in a valid format",
            "D8PaymentMethod must be \"Cheque\", \"Debit/Credit Card\" or left blank",
            "PetitionerSolicitor must be \"Yes\" or \"No\"",
            "PetitionerSolicitorAddressPostCode is usually 6 or 7 characters long",
            "PetitionerSolicitorPhone is not in a valid format",
            "PetitionerSolicitorEmail is not in a valid format",
            "D8PetitionerCorrespondenceUseHomeAddress must be \"Yes\" or \"No\"",
            "D8PetitionerCorrespondencePostcode is usually 6 or 7 characters long"
        ));
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = asList(
            new OcrDataField("PetitionerSolicitorName", ""),
            new OcrDataField("D8SolicitorReference", ""),
            new OcrDataField("PetitionerSolicitorFirm", ""),
            new OcrDataField("PetitionerSolicitorAddressPostCode", ""),
            new OcrDataField("PetitionerSolicitorPhone", ""),
            new OcrDataField("PetitionerSolicitorEmail", ""),
            new OcrDataField("D8PetitionerCorrespondencePostcode", ""),
            new OcrDataField("PetitionerSolicitorAddressStreet", ""),
            new OcrDataField("PetitionerSolicitorAddressTown", ""),
            new OcrDataField("PetitionerSolicitorAddressCounty", ""),
            new OcrDataField("D8PetitionerCorrespondenceAddressStreet", ""),
            new OcrDataField("D8PetitionerCorrespondenceAddressTown", ""),
            new OcrDataField("D8PetitionerCorrespondenceAddressCounty", "")
        );

        listOfAllMandatoryFields.addAll(nonMandatoryFieldsWithEmptyValues);
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);
        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassIfUsingValidHelpWithFeesNumberAndNoOtherPaymentMethod() {
        listOfAllMandatoryFields.remove(validD8paymentMethod);
        listOfAllMandatoryFields.add(
            new OcrDataField("D8HelpWithFeesReferenceNumber", "123456"));
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfUsingInvalidHelpWithFeesNumberAndNoOtherPaymentMethod() {
        listOfAllMandatoryFields.remove(validD8paymentMethod);
        listOfAllMandatoryFields.add(new OcrDataField("D8HelpWithFeesReferenceNumber", "ABCDEF"));
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItem("D8HelpWithFeesReferenceNumber is usually 6 digits"));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfUsingMultipleValidPaymentMethods() {
        listOfAllMandatoryFields.add(new OcrDataField("D8HelpWithFeesReferenceNumber", "123456"));
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItem(
            "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated"
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfNoPaymentMethodsProvided() {
        listOfAllMandatoryFields.remove(validD8paymentMethod);
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), hasItem(
            "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value"
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassPhoneNumberMatchingCustomValidationRules() {
        String[] validPhoneNumbers = {"07231334455", "+44 20 8356 3333", "01213334444", "0909 8790000",
            "(0131) 496 0645", "0044 117496 0813", "07700 90 09 99"};
        for (String validPhoneNumber : validPhoneNumbers) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerPhoneNumber", validPhoneNumber));
            mandatoryFieldsCopy.add(new OcrDataField("D8RespondentPhoneNumber", validPhoneNumber));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorPhone", validPhoneNumber));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(mandatoryFieldsCopy);

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldFailPhoneNumberNotMatchingCustomValidationRules() {
        String[] invalidPhoneNumbers = {"0723155", "+44 2083", "044(121)", "newphonewhodis", "se14Tp"};
        for (String invalidPhoneNumber : invalidPhoneNumbers) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerPhoneNumber", invalidPhoneNumber));
            mandatoryFieldsCopy.add(new OcrDataField("D8RespondentPhoneNumber", invalidPhoneNumber));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorPhone", invalidPhoneNumber));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(mandatoryFieldsCopy);

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getWarnings(), hasItems(
                "D8PetitionerPhoneNumber is not in a valid format",
                "D8RespondentPhoneNumber is not in a valid format",
                "PetitionerSolicitorPhone is not in a valid format"
            ));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldPassPetitionerEmailsMatchingCustomValidationRules() {
        String[] validEmailAddresses = {"aaa@gmail.com", "john.doe@mail.pl", "akjl2489.rq23@a.co.uk"};
        for (String validEmailAddress : validEmailAddresses) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerEmail", validEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorEmail", validEmailAddress));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(mandatoryFieldsCopy);

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldFailPetitionerEmailsNotMatchingCustomValidationRules() {
        String[] invalidEmailAddresses = {"aaa@gmail.", "john.doe@mai", "akjl2489.rq23@", " @ ", "adada@sfwe"};
        for (String invalidEmailAddress : invalidEmailAddresses) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerEmail", invalidEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorEmail", invalidEmailAddress));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(mandatoryFieldsCopy);

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getWarnings(), hasItems(
                "D8PetitionerEmail is not in a valid format",
                "PetitionerSolicitorEmail is not in a valid format"
            ));
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
        assertThat(validationResult.getWarnings(), hasItem("D8ReasonForDivorceSeparationDate must be a valid date"));
    }
}