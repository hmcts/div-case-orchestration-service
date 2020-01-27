package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;

public class AosOffline5yrSepCaseValidatorTest {

    private final AosOffline5yrSepCaseValidator classUnderTest = new AosOffline5yrSepCaseValidator();
    private List<OcrDataField> listOfAllMandatoryFields;

    @Before
    public void setup() {
        List<OcrDataField> listOfAllMandatoryFieldsImmutable = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
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
            "Mandatory field \"CaseNumber\" is missing",
            "Mandatory field \"AOSReasonForDivorce\" is missing",
            "Mandatory field \"RespConfirmReadPetition\" is missing",
            "Mandatory field \"DateRespReceivedDivorceApplication\" is missing",
            "Mandatory field \"RespWillDefendDivorce\" is missing",
            "Mandatory field \"RespConsiderFinancialSituation\" is missing",
            "Mandatory field \"RespJurisdictionAgree\" is missing",
            "Mandatory field \"RespLegalProceedingsExist\" is missing",
            "Mandatory field \"RespAgreeToCosts\" is missing"
        ));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("CaseNumber", ""),
            new OcrDataField("AOSReasonForDivorce", ""),
            new OcrDataField("RespConfirmReadPetition", ""),
            new OcrDataField("DateRespReceivedDivorceApplication", ""),
            new OcrDataField("RespHardshipDefenseResponse", ""),
            new OcrDataField("RespWillDefendDivorce", ""),
            new OcrDataField("RespConsiderFinancialSituation", ""),
            new OcrDataField("RespJurisdictionAgree", ""),
            new OcrDataField("RespLegalProceedingsExist", ""),
            new OcrDataField("RespAgreeToCosts", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"CaseNumber\" is missing",
            "Mandatory field \"AOSReasonForDivorce\" is missing",
            "Mandatory field \"RespConfirmReadPetition\" is missing",
            "Mandatory field \"DateRespReceivedDivorceApplication\" is missing",
            "Mandatory field \"RespWillDefendDivorce\" is missing",
            "Mandatory field \"RespConsiderFinancialSituation\" is missing",
            "Mandatory field \"RespJurisdictionAgree\" is missing",
            "Mandatory field \"RespLegalProceedingsExist\" is missing",
            "Mandatory field \"RespAgreeToCosts\" is missing"
        ));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Que?"),
            new OcrDataField("RespHardshipDefenseResponse", "Possibly"),
            new OcrDataField("RespWillDefendDivorce", "Yes"),
            new OcrDataField("RespConsiderFinancialSituation", "Possibly"),
            new OcrDataField("RespJurisdictionAgree", "Possibly"),
            new OcrDataField("RespLegalProceedingsExist", "Not telling"),
            new OcrDataField("RespAgreeToCosts", "Possibly"),
            new OcrDataField("RespStatementOfTruth", "No")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "AOSReasonForDivorce must be \"5 years separation\"",
            "RespConfirmReadPetition must be \"Yes\" or \"No\"",
            "DateRespReceivedDivorceApplication must be a valid 8 digit date",
            "RespHardshipDefenseResponse must be \"Yes\" or \"No\"",
            "RespWillDefendDivorce must be \"Proceed\" or \"Defend\"",
            "RespConsiderFinancialSituation must be \"Yes\" or \"No\"",
            "RespJurisdictionAgree must be \"Yes\" or \"No\"",
            "RespLegalProceedingsExist must be \"Yes\" or \"No\"",
            "RespAgreeToCosts must be \"Yes\" or \"No\""
        ));
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = asList(
            new OcrDataField("RespJurisdictionDisagreeReason", ""),
            new OcrDataField("RespLegalProceedingsDescription", "")
        );

        listOfAllMandatoryFields.addAll(nonMandatoryFieldsWithEmptyValues);
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldOnlyValidateRespStatementofTruthSignedDateIfPresent() {

        List<OcrDataField> testFields = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12 December 2019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(testFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespStatementofTruthSignedDate must be a valid 8 digit date"
        ));
    }

    @Test
    public void shouldNotValidateRespStatementofTruthSignedDateIfNotPresent() {
        List<OcrDataField> testFields = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(testFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassIfRespJurisdictionIsAgreedAndNoReasonForDisagreementProvided() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespJurisdictionDisagreeReason", ""),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfRespJurisdictionIsAgreedButReasonForDisagreementProvided() {

        listOfAllMandatoryFields.add(
            new OcrDataField("RespJurisdictionDisagreeReason", "Here is my reason for disagreeing with the jurisdiction"));
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No"
        ));
    }

    @Test
    public void shouldPassIfRespJurisdictionIsNotAgreedAndReasonForDisagreementIsProvided() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "No"),
            new OcrDataField("RespJurisdictionDisagreeReason", "Here is my reason for disagreeing with the jurisdictiosssn"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfRespJurisdictionIsNotAgreedAndNoReasonForDisagreementIsProvided() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "No"),
            new OcrDataField("RespJurisdictionDisagreeReason", ""),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No"
        ));
    }

    @Test
    public void shouldPassIfRespLegalProceedingsDoNotExistAndRespLegalProceedingsDescriptionExists() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "Yes"),
            new OcrDataField("RespLegalProceedingsDescription", "My description of my other legal proceedings"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassIfRespLegalProceedingsDoNotExistAndNoRespLegalProceedingsDescriptionIsGiven() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassIfRespLegalProceedingsExistAndRespLegalProceedingsDescriptionIsGiven() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "Yes"),
            new OcrDataField("RespLegalProceedingsDescription", "My description of my other legal proceedings"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12102019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailIfRespLegalProceedingsExistAndRespLegalProceedingsDescriptionIsNotGiven() {

        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "5 years separation"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespHardshipDefenseResponse", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespConsiderFinancialSituation", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "Yes"),
            new OcrDataField("RespAgreeToCosts", "Yes")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No"
        ));
    }

    @Test
    public void shouldFailIfRespLegalProceedingsDoNotExistAndRespLegalProceedingsDescriptionIsNotGiven() {

        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = asList(
            new OcrDataField("RespLegalProceedingsDescription", "My description of my other legal proceedings")
        );

        listOfAllMandatoryFields.addAll(nonMandatoryFieldsWithEmptyValues);
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No"
        ));
    }
}