package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;

public class D8FormValidatorTest {

    public static final String INVALID_POSTCODE = "SW15 5PUX";
    public static final String INVALID_EMAIL = "nece28ssito@no.";
    public static final String NOT_YES_OR_NO = "Tomato";

    private final D8FormValidator classUnderTest = new D8FormValidator();
    private List<OcrDataField> listOfAllMandatoryFields;
    private OcrDataField validD8paymentMethod = new OcrDataField("D8PaymentMethod", "Cheque");

    @Before
    public void setup() {
        listOfAllMandatoryFields = new ArrayList<>(asList(
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
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "Yes"),
            new OcrDataField("D8PetitionerHomeAddressStreet", "19 West Park Road"),
            new OcrDataField("D8PetitionerHomeAddressTown", "Smethwick"),
            new OcrDataField("D8PetitionerHomeAddressCounty", "West Midlands"),
            new OcrDataField("D8PetitionerNameDifferentToMarriageCert", "No"),
            new OcrDataField("D8RespondentHomeAddressStreet", "18 West Park Road"),
            new OcrDataField("D8RespondentHomeAddressTown", "Smethwick"),
            new OcrDataField("D8RespondentHomeAddressCounty", "West Midlands"),
            new OcrDataField("D8RespondentPostcode", "WE1 MI2"),
            new OcrDataField("D8RespondentCorrespondenceSendToSol", "No"),
            new OcrDataField("D8MarriedInUk", "Yes"),
            new OcrDataField("D8ApplicationToIssueWithoutCertificate", "Yes"),
            new OcrDataField("D8MarriagePlaceOfMarriage", "Slough"),
            new OcrDataField("D8MarriageDateDay", "19"),
            new OcrDataField("D8MarriageDateMonth", "03"),
            new OcrDataField("D8MarriageDateYear", "2006"),
            new OcrDataField("D8MarriageCertificateCorrect", "Yes")
        ));
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
        assertThat(validationResult.getWarnings(), hasItems(Stream.of(
            "D8PetitionerFirstName",
            "D8PetitionerLastName",
            "D8LegalProcess",
            "D8ScreenHasMarriageCert",
            "D8RespondentFirstName",
            "D8RespondentLastName",
            "D8MarriagePetitionerName",
            "D8MarriageRespondentName",
            "D8PetitionerNameChangedHow",
            "D8PetitionerContactDetailsConfidential",
            "D8PetitionerPostCode",
            "PetitionerSolicitor",
            "D8PetitionerCorrespondenceUseHomeAddress",
            "D8PetitionerHomeAddressStreet",
            "D8PetitionerHomeAddressTown",
            "D8PetitionerHomeAddressCounty",
            "D8PetitionerNameDifferentToMarriageCert",
            "D8RespondentHomeAddressStreet",
            "D8RespondentHomeAddressTown",
            "D8RespondentHomeAddressCounty",
            "D8RespondentPostcode",
            "D8RespondentCorrespondenceSendToSol",
            "D8MarriedInUk",
            "D8ApplicationToIssueWithoutCertificate",
            "D8MarriageDateDay",
            "D8MarriageDateMonth",
            "D8MarriageDateYear",
            "D8MarriageCertificateCorrect"
        )
            .map(mandatoryFieldIsMissing)
            .toArray(String[]::new)));
    }
    
    @Test
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(Stream.of(
            "D8PetitionerFirstName",
            "D8PetitionerLastName",
            "D8RespondentFirstName",
            "D8RespondentLastName",
            "D8MarriagePetitionerName",
            "D8MarriageRespondentName",
            "D8PetitionerNameChangedHow",
            "D8PetitionerPostCode",
            "D8PetitionerContactDetailsConfidential",
            "PetitionerSolicitor",
            "D8PetitionerCorrespondenceUseHomeAddress",
            "D8PetitionerHomeAddressStreet",
            "D8PetitionerHomeAddressTown",
            "D8PetitionerHomeAddressCounty",
            "D8PetitionerNameDifferentToMarriageCert",
            "D8RespondentHomeAddressStreet",
            "D8RespondentHomeAddressTown",
            "D8RespondentHomeAddressCounty",
            "D8RespondentPostcode",
            "D8RespondentCorrespondenceSendToSol",
            "D8MarriedInUk",
            "D8ApplicationToIssueWithoutCertificate",
            "D8MarriageCertificateCorrect",
            "D8MarriageDateDay",
            "D8MarriageDateMonth",
            "D8MarriageDateYear"
        )
            .map(emptyValueOcrDataField)
            .collect(Collectors.toList()));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(Stream.of(
            "D8PetitionerFirstName",
            "D8PetitionerLastName",
            "D8RespondentFirstName",
            "D8RespondentLastName",
            "D8MarriagePetitionerName",
            "D8MarriageRespondentName",
            "D8PetitionerNameChangedHow",
            "D8PetitionerContactDetailsConfidential",
            "D8PetitionerPostCode",
            "PetitionerSolicitor",
            "D8PetitionerCorrespondenceUseHomeAddress",
            "D8PetitionerHomeAddressStreet",
            "D8PetitionerHomeAddressTown",
            "D8PetitionerHomeAddressCounty",
            "D8PetitionerNameDifferentToMarriageCert",
            "D8RespondentHomeAddressStreet",
            "D8RespondentHomeAddressTown",
            "D8RespondentHomeAddressCounty",
            "D8RespondentPostcode",
            "D8RespondentCorrespondenceSendToSol",
            "D8MarriedInUk",
            "D8ApplicationToIssueWithoutCertificate",
            "D8MarriageCertificateCorrect",
            "D8MarriageDateDay",
            "D8MarriageDateMonth",
            "D8MarriageDateYear"
        )
            .map(mandatoryFieldIsMissing)
            .toArray(String[]::new)));
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
            new OcrDataField("D8PetitionerPostCode", INVALID_POSTCODE),
            new OcrDataField("D8RespondentPhoneNumber", "01213344"),
            new OcrDataField("D8ReasonForDivorceSeparationDate", "this should be a date"),
            new OcrDataField("PetitionerSolicitor", "I don't have one"),
            new OcrDataField("PetitionerSolicitorAddressPostCode", "m4 2a"),
            new OcrDataField("PetitionerSolicitorPhone", "07700900four"),
            new OcrDataField("PetitionerSolicitorEmail", INVALID_EMAIL),
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "Where"),
            new OcrDataField("D8PetitionerCorrespondencePostcode", INVALID_POSTCODE),
            new OcrDataField("D8ReasonForDivorceAdultery3rdPartyPostCode", INVALID_POSTCODE),
            new OcrDataField("D8PetitionerNameDifferentToMarriageCert", NOT_YES_OR_NO),
            new OcrDataField("D8RespondentPostcode", INVALID_POSTCODE),
            new OcrDataField("D8RespondentEmailAddress", INVALID_EMAIL),
            new OcrDataField("D8RespondentCorrespondenceSendToSol", NOT_YES_OR_NO),
            new OcrDataField("D8RespondentSolicitorAddressPostCode", INVALID_POSTCODE),
            new OcrDataField("D8MarriedInUk", "does the isle of man count?"),
            new OcrDataField("D8ApplicationToIssueWithoutCertificate", "check"),
            new OcrDataField("D8MarriageCertificateCorrect", "fake")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "D8LegalProcess must be \"Divorce\", \"Dissolution\" or \"Judicial (separation)\"",
            "D8ScreenHasMarriageCert must be \"True\"",
            "D8CertificateInEnglish must be \"True\" or left blank",
            notInAValidFormat("D8PetitionerPhoneNumber"),
            mustBeYesOrNo("D8PetitionerNameChangedHow"),
            notInAValidFormat("D8PetitionerEmail"),
            postcodeIsUsually6or7CharactersLong("D8PetitionerPostCode"),
            mustBeYesOrNo("D8PetitionerContactDetailsConfidential"),
            "D8CertificateInEnglish must be \"True\" or left blank",
            "D8ReasonForDivorceSeparationDate must be a valid date",
            notInAValidFormat("D8PetitionerPhoneNumber"),
            "D8PaymentMethod must be \"Cheque\", \"Debit/Credit Card\" or left blank",
            mustBeYesOrNo("PetitionerSolicitor"),
            postcodeIsUsually6or7CharactersLong("PetitionerSolicitorAddressPostCode"),
            notInAValidFormat("PetitionerSolicitorPhone"),
            notInAValidFormat("PetitionerSolicitorEmail"),
            mustBeYesOrNo("D8PetitionerCorrespondenceUseHomeAddress"),
            postcodeIsUsually6or7CharactersLong("D8PetitionerCorrespondencePostcode"),
            postcodeIsUsually6or7CharactersLong("D8ReasonForDivorceAdultery3rdPartyPostCode"),
            mustBeYesOrNo("D8PetitionerNameDifferentToMarriageCert"),
            postcodeIsUsually6or7CharactersLong("D8RespondentPostcode"),
            notInAValidFormat("D8RespondentEmailAddress"),
            mustBeYesOrNo("D8RespondentCorrespondenceSendToSol"),
            postcodeIsUsually6or7CharactersLong("D8RespondentSolicitorAddressPostCode"),
            mustBeYesOrNo("D8MarriedInUk"),
            mustBeYesOrNo("D8ApplicationToIssueWithoutCertificate"),
            mustBeYesOrNo("D8MarriageCertificateCorrect")
        ));
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = Stream.of(
            "PetitionerSolicitorName",
            "D8SolicitorReference",
            "PetitionerSolicitorFirm",
            "PetitionerSolicitorAddressPostCode",
            "PetitionerSolicitorPhone",
            "PetitionerSolicitorEmail",
            "PetitionerSolicitorAddressStreet",
            "PetitionerSolicitorAddressTown",
            "PetitionerSolicitorAddressCounty",
            "D8PetitionerCorrespondenceAddressStreet",
            "D8PetitionerCorrespondenceAddressTown",
            "D8PetitionerCorrespondenceAddressCounty",
            "D8ReasonForDivorceAdultery3rdPartyFName",
            "D8ReasonForDivorceAdultery3rdPartyLName",
            "D8ReasonForDivorceAdultery3rdPartyAddressStreet",
            "D8ReasonForDivorceAdultery3rdPartyTown",
            "D8ReasonForDivorceAdultery3rdPartyCounty",
            "D8ReasonForDivorceAdultery3rdPartyPostCode",
            "RespNameDifferentToMarriageCertExplain",
            "D8RespondentEmailAddress",
            "D8RespondentSolicitorName",
            "D8RespondentSolicitorReference",
            "D8RespondentSolicitorCompany",
            "D8RespondentSolicitorAddressStreet",
            "D8RespondentSolicitorAddressTown",
            "D8RespondentSolicitorAddressCounty",
            "D8RespondentSolicitorAddressPostCode"
        )
            .map(emptyValueOcrDataField)
            .collect(Collectors.toList());

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
    public void shouldPassEmailsMatchingCustomValidationRules() {
        String[] validEmailAddresses = {"aaa@gmail.com", "john.doe@mail.pl", "akjl2489.rq23@a.co.uk"};
        for (String validEmailAddress : validEmailAddresses) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerEmail", validEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorEmail", validEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("D8RespondentEmailAddress", validEmailAddress));

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(mandatoryFieldsCopy);

            assertThat(validationResult.getStatus(), is(SUCCESS));
            assertThat(validationResult.getWarnings(), is(emptyList()));
            assertThat(validationResult.getErrors(), is(emptyList()));
        }
    }

    @Test
    public void shouldFailEmailsNotMatchingCustomValidationRules() {
        String[] invalidEmailAddresses = {"aaa@gmail.", "john.doe@mai", "akjl2489.rq23@", " @ ", "adada@sfwe"};
        for (String invalidEmailAddress : invalidEmailAddresses) {
            List<OcrDataField> mandatoryFieldsCopy = new ArrayList<>(listOfAllMandatoryFields);
            mandatoryFieldsCopy.add(new OcrDataField("D8PetitionerEmail", invalidEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("PetitionerSolicitorEmail", invalidEmailAddress));
            mandatoryFieldsCopy.add(new OcrDataField("D8RespondentEmailAddress", invalidEmailAddress));

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
    public void shouldFailValidationWithWrongLeapYearReasonForDivorceSeparationDate() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8ReasonForDivorceSeparationDate", "29/02/2019")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItem("D8ReasonForDivorceSeparationDate must be a valid date"));
    }

    @Test
    public void shouldNotProduceWarningsForPlaceOfMarriageWhenBothPrerequsitesAreEmpty() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriedInUk", ""),
            new OcrDataField("D8ApplicationToIssueWithoutCertificate", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), not(hasItem(
            "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                + "\"D8ApplicationToIssueWithoutCertificate\""
            ))
        );
    }

    @Test
    public void shouldFailIfD8PetitionerCorrespondenceAddressIsEmptyAndD8PetitionerCorrespondenceUseHomeAddressIsNo() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "No"),
            new OcrDataField("D8PetitionerCorrespondenceAddressStreet", ""),
            new OcrDataField("D8PetitionerCorrespondenceAddressTown", ""),
            new OcrDataField("D8PetitionerCorrespondenceAddressCounty", ""),
            new OcrDataField("D8PetitionerCorrespondencePostcode", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "\"D8PetitionerCorrespondenceAddressStreet\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondenceAddressTown\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondenceAddressCounty\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondencePostcode\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\""
        ));
    }

    @Test
    public void shouldNotProduceWarningsForPlaceOfMarriageWhenBothPrerequsitesAreEmptyAndPlaceOfMarriageExists() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriedInUk", ""),
            new OcrDataField("D8ApplicationToIssueWithoutCertificate", ""),
            new OcrDataField("D8MarriagePlaceOfMarriage", "Slough")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), not(hasItem(
            "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                + "\"D8ApplicationToIssueWithoutCertificate\""
            ))
        );
    }

    @Test
    public void shouldNotProduceWarningsForPlaceOfMarriageIfOnlyOnePrerequsiteIsNoOrNullAndPlaceOfMarriageExists() {
        Map<String, String> notMarriedInUkOnly = new HashMap<String, String>() {{
                put("D8MarriedInUk", "No");
            }};

        Map<String, String> issueWithoutCertOnly = new HashMap<String, String>() {{
                put("D8ApplicationToIssueWithoutCertificate", "Yes");
            }};

        List<Map<String, String>> incompletePrerequisites = asList(notMarriedInUkOnly, issueWithoutCertOnly);

        for (Map<String, String> incompleteCombination : incompletePrerequisites) {

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8MarriedInUk", incompleteCombination.get("D8MarriedInUk")),
                new OcrDataField("D8ApplicationToIssueWithoutCertificate", incompleteCombination.get("D8ApplicationToIssueWithoutCertificate")),
                new OcrDataField("D8MarriagePlaceOfMarriage", "Slough")
            ));

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getErrors(), is(emptyList()));
            assertThat(validationResult.getWarnings(), not(hasItem(
                "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                    + "\"D8ApplicationToIssueWithoutCertificate\""
                ))
            );
        }
    }

    @Test
    public void shouldProduceWarningsForPlaceOfMarriageForAllPrerequisiteCombinationsAndNoPlaceOfMarriage() {
        Map<String, String> marriedInUkNotWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "Yes");
                put("D8ApplicationToIssueWithoutCertificate", "No");
            }};

        Map<String, String> notMarriedInUkWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "No");
                put("D8ApplicationToIssueWithoutCertificate", "Yes");
            }};

        Map<String, String> notMarriedInUkNotWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "No");
                put("D8ApplicationToIssueWithoutCertificate", "No");
            }};

        Map<String, String> marriedInUkWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "Yes");
                put("D8ApplicationToIssueWithoutCertificate", "Yes");
            }};

        List<Map<String, String>> invalidMarriedInUkIssueWithoutCertCombinations =
            asList(marriedInUkNotWithoutCert, notMarriedInUkWithoutCert, notMarriedInUkNotWithoutCert, marriedInUkWithoutCert);

        for (Map<String, String> invalidCombination : invalidMarriedInUkIssueWithoutCertCombinations) {

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8MarriedInUk", invalidCombination.get("D8MarriedInUk")),
                new OcrDataField("D8ApplicationToIssueWithoutCertificate", invalidCombination.get("D8ApplicationToIssueWithoutCertificate")),
                new OcrDataField("D8MarriagePlaceOfMarriage", "")
            ));

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getErrors(), is(emptyList()));
            assertThat(validationResult.getWarnings(), hasItem(
                "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                    + "\"D8ApplicationToIssueWithoutCertificate\""
            ));
        }
    }

    @Test
    public void shouldNotProduceWarningsForPlaceOfMarriageForAllPrerequisiteCombinationsAndPlaceOfMarriageExists() {
        Map<String, String> marriedInUkNotWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "Yes");
                put("D8ApplicationToIssueWithoutCertificate", "No");
            }};

        Map<String, String> notMarriedInUkWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "No");
                put("D8ApplicationToIssueWithoutCertificate", "Yes");
            }};

        Map<String, String> notMarriedInUkNotWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "No");
                put("D8ApplicationToIssueWithoutCertificate", "No");
            }};

        Map<String, String> marriedInUkWithoutCert = new HashMap<String, String>() {{
                put("D8MarriedInUk", "Yes");
                put("D8ApplicationToIssueWithoutCertificate", "Yes");
            }};

        List<Map<String, String>> validMarriedInUkIssueWithoutCertCombinations =
            asList(marriedInUkNotWithoutCert, notMarriedInUkWithoutCert, notMarriedInUkNotWithoutCert, marriedInUkWithoutCert);

        for (Map<String, String> correctCombination : validMarriedInUkIssueWithoutCertCombinations) {

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8MarriedInUk", correctCombination.get("D8MarriedInUk")),
                new OcrDataField("D8ApplicationToIssueWithoutCertificate", correctCombination.get("D8ApplicationToIssueWithoutCertificate")),
                new OcrDataField("D8MarriagePlaceOfMarriage", "Slough")
            ));

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getErrors(), is(emptyList()));
            assertThat(validationResult.getWarnings(), not(hasItem(
                "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                    + "\"D8ApplicationToIssueWithoutCertificate\""
                ))
            );
        }
    }

    @Test
    public void shouldNotProduceWarningsForD8MarriageCertificateCorrectExplainIfMarriageCertificateCorrectDoesNotExist() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageCertificateCorrectExplain", "no reasons")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItem("Mandatory field \"D8MarriageCertificateCorrect\" is missing"),
            not(hasItems("If D8MarriageCertificateCorrect is \"Yes\", then D8MarriageCertificateCorrectExplain should be empty",
                "If D8MarriageCertificateCorrect is \"No\", then D8MarriageCertificateCorrectExplain should not be empty"
            ))
        ));
    }

    @Test
    public void shouldNotProduceWarningsForD8MarriageCertificateCorrectExplainIfPrerequisitesCombinationsAreValid() {
        Map<String, String> marriageCertCorrectEmptyExplain = new HashMap<String, String>() {{
                put("D8MarriageCertificateCorrect", "Yes");
                put("D8MarriageCertificateCorrectExplain", "");
            }};

        Map<String, String> marriageCertCorrectNullExplain = new HashMap<String, String>() {{
                put("D8MarriageCertificateCorrect", "Yes");
            }};

        Map<String, String> marriageCertNotCorrectExplain = new HashMap<String, String>() {{
                put("D8MarriageCertificateCorrect", "No");
                put("D8MarriageCertificateCorrectExplain", "insert reason here");
            }};

        List<Map<String, String>> validMarriageCertOptions =
            asList(marriageCertCorrectEmptyExplain, marriageCertCorrectNullExplain, marriageCertNotCorrectExplain);

        for (Map<String, String> marriageCertCorrectCombination : validMarriageCertOptions) {

            OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
                new OcrDataField("D8MarriageCertificateCorrect", marriageCertCorrectCombination.get("D8MarriageCertificateCorrect")),
                new OcrDataField("D8MarriageCertificateCorrectExplain", marriageCertCorrectCombination.get("D8MarriageCertificateCorrectExplain"))
            ));

            assertThat(validationResult.getStatus(), is(WARNINGS));
            assertThat(validationResult.getErrors(), is(emptyList()));
            assertThat(validationResult.getWarnings(), not(hasItems(
                "If D8MarriageCertificateCorrect is \"Yes\", then D8MarriageCertificateCorrectExplain should be empty",
                "If D8MarriageCertificateCorrect is \"No\", then D8MarriageCertificateCorrectExplain should not be empty"
                ))
            );
        }
    }

    @Test
    public void shouldProduceWarningsForEmptyD8MarriageCertificateCorrectExplainIfMarriageCertificateCorrectNo() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageCertificateCorrect", "No")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItem("If D8MarriageCertificateCorrect is \"No\", then D8MarriageCertificateCorrectExplain should not be empty"),
            not(hasItem("If D8MarriageCertificateCorrect is \"Yes\", then D8MarriageCertificateCorrectExplain should be empty")
            ))
        );
    }

    @Test
    public void shouldProduceWarningsForNonEmptyD8MarriageCertificateCorrectExplainIfMarriageCertificateCorrectYes() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageCertificateCorrect", "Yes"),
            new OcrDataField("D8MarriageCertificateCorrectExplain", "insert reason here")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItem("If D8MarriageCertificateCorrect is \"Yes\", then D8MarriageCertificateCorrectExplain should be empty"),
            not(hasItem("If D8MarriageCertificateCorrect is \"No\", then D8MarriageCertificateCorrectExplain should not be empty")
            ))
        );
    }

    @Test
    public void shouldFailIfD8PetitionerCorrespondenceAddressIsNullAndD8PetitionerCorrespondenceUseHomeAddressIsNo() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "No")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "\"D8PetitionerCorrespondenceAddressStreet\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondenceAddressTown\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondenceAddressCounty\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\"",
            "\"D8PetitionerCorrespondencePostcode\" should not be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"No\""
        ));
    }

    @Test
    public void shouldFailValidationIfNotAllMarriageDateComponentsArePresent() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageDateDay", "05"),
            new OcrDataField("D8MarriageDateYear", "2014")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItems(
                "Mandatory field \"D8MarriageDateMonth\" is missing",
                "Not all date components are present"),
            not(hasItems(
                "D8MarriageDateDay is invalid",
                "D8MarriageDateYear needs to be after 1900 and have 4 digits e.g. 2011"
            ))
        ));
    }

    @Test
    public void shouldFailValidationIfMarriageDateMonthComponentIsNotParsable() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageDateDay", "28"),
            new OcrDataField("D8MarriageDateMonth", "Feb"),
            new OcrDataField("D8MarriageDateYear", "2014")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItem(
                "One or more of D8MarriageDateDay, D8MarriageDateMonth, D8MarriageDateYear "
                    + "contain invalid characters that can't be converted into a date"),
            not(hasItems(
                "Mandatory field \"D8MarriageDateDay\" is missing",
                "Mandatory field \"D8MarriageDateMonth\" is missing",
                "Mandatory field \"D8MarriageDateYear\" is missing",
                "Not all date components are present")
            ))
        );
    }

    @Test
    public void shouldFailIfD8PetitionerCorrespondenceAddressIsNotEmptyAndD8PetitionerCorrespondenceUseHomeAddressIsYes() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8PetitionerCorrespondenceUseHomeAddress", "Yes"),
            new OcrDataField("D8PetitionerCorrespondenceAddressStreet", "20 correspondence road"),
            new OcrDataField("D8PetitionerCorrespondenceAddressTown", "Correspondencetown"),
            new OcrDataField("D8PetitionerCorrespondenceAddressCounty", "South Midlands"),
            new OcrDataField("D8PetitionerCorrespondencePostcode", "SE12BP")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "\"D8PetitionerCorrespondenceAddressStreet\" should be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"Yes\"",
            "\"D8PetitionerCorrespondenceAddressTown\" should be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"Yes\"",
            "\"D8PetitionerCorrespondenceAddressCounty\" should be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"Yes\"",
            "\"D8PetitionerCorrespondencePostcode\" should be empty if \"D8PetitionerCorrespondenceUseHomeAddress\" is \"Yes\""
        ));
    }

    @Test
    public void shouldFailValidationIfMarriageDateInvalidLeapYear() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("D8MarriageDateDay", "29"),
            new OcrDataField("D8MarriageDateMonth", "02"),
            new OcrDataField("D8MarriageDateYear", "2013")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItem(
                "Invalid date made up of D8MarriageDateDay, D8MarriageDateMonth, D8MarriageDateYear"),
            not(hasItems(
                "Not all date components are present",
                "D8MarriageDateMonth is not a valid month e.g. 03 for March")
            ))
        );
    }

    private Function<String, String> mandatoryFieldIsMissing = fieldName -> String.format("Mandatory field \"%s\" is missing", fieldName);

    private String mustBeYesOrNo(String fieldName) {
        return String.format("%s must be \"Yes\" or \"No\"", fieldName);
    }

    private String postcodeIsUsually6or7CharactersLong(String fieldName) {
        return String.format("%s is usually 6 or 7 characters long", fieldName);
    }

    private String notInAValidFormat(String fieldName) {
        return String.format("%s is not in a valid format", fieldName);
    }

    private Function<String, OcrDataField> emptyValueOcrDataField = fieldName -> new OcrDataField(fieldName, "");
}
