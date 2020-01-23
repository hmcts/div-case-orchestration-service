package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;

public class AosOfflineAdulteryRespCaseValidatorTest {

    private final AosOfflineAdulteryRespCaseValidator classUnderTest = new AosOfflineAdulteryRespCaseValidator();
    private List<OcrDataField> listOfAllMandatoryFields;

    @Before
    public void setup() {
        List<OcrDataField> listOfAllMandatoryFieldsImmutable = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
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
            "Mandatory field \"RespAOSAdultery\" is missing",
            "Mandatory field \"RespWillDefendDivorce\" is missing",
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
            new OcrDataField("RespAOSAdultery", ""),
            new OcrDataField("RespWillDefendDivorce", ""),
            new OcrDataField("RespJurisdictionAgree", ""),
            new OcrDataField("RespLegalProceedingsExist", ""),
            new OcrDataField("RespAgreeToCosts", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItems(
                "Mandatory field \"CaseNumber\" is missing",
                "Mandatory field \"AOSReasonForDivorce\" is missing",
                "Mandatory field \"RespConfirmReadPetition\" is missing",
                "Mandatory field \"DateRespReceivedDivorceApplication\" is missing",
                "Mandatory field \"RespAOSAdultery\" is missing",
                "Mandatory field \"RespWillDefendDivorce\" is missing",
                "Mandatory field \"RespJurisdictionAgree\" is missing",
                "Mandatory field \"RespLegalProceedingsExist\" is missing",
                "Mandatory field \"RespAgreeToCosts\" is missing"
            )
        ));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(asList(
            new OcrDataField("AOSReasonForDivorce", "2 years separation with consent"),
            new OcrDataField("RespConfirmReadPetition", "Que?"),
            new OcrDataField("DateRespReceivedDivorceApplication", "01/11/2019"),
            new OcrDataField("RespAOSAdultery", "Wat"),
            new OcrDataField("RespWillDefendDivorce", "No"),
            new OcrDataField("RespJurisdictionAgree", "Possibly"),
            new OcrDataField("RespLegalProceedingsExist", "Not telling"),
            new OcrDataField("RespAgreeToCosts", "Possibly"),
            new OcrDataField("RespStatementOfTruth", "No")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "AOSReasonForDivorce must be \"Adultery\"",
            "RespConfirmReadPetition must be \"Yes\" or \"No\"",
            "DateRespReceivedDivorceApplication must be a valid 8 digit date",
            "RespAOSAdultery must be \"Yes\" or \"No\"",
            "RespWillDefendDivorce must be \"Proceed\" or \"Defend\"",
            "RespJurisdictionAgree must be \"Yes\" or \"No\"",
            "RespLegalProceedingsExist must be \"Yes\" or \"No\"",
            "RespAgreeToCosts must be \"Yes\" or \"No\"",
            "RespStatementOfTruth must be \"Yes\" or left blank"
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
    public void shouldOnlyValidateRespStatementOfTruthSignedDateIfPresent() {
        List<OcrDataField> testFields = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespLegalProceedingsExist", "No"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "12 December 2019")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(testFields);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), allOf(
            hasItems(
                "RespStatementofTruthSignedDate must be a valid 8 digit date"
            )
        ));
    }

    @Test
    public void shouldNotValidateRespStatementOfTruthSignedDateIfNotPresent() {
        List<OcrDataField> testFields = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
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
    public void shouldFailIfRespJurisdictionIsAgreedButReasonForDisagreementProvided() {
        listOfAllMandatoryFields.add(
            new OcrDataField("RespJurisdictionDisagreeReason", "Something else")
        );

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
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
            new OcrDataField("RespJurisdictionAgree", "No"),
            new OcrDataField("RespJurisdictionDisagreeReason", "Here is my reason for disagreeing with the jurisdiction"),
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
            new OcrDataField("RespJurisdictionAgree", "No"),
            new OcrDataField("RespJurisdictionDisagreeReason", "")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No"
        ));
    }

    @Test
    public void shouldPassIfRespLegalProceedingsDoNotExistAndRespLegalProceedingsDescriptionExist() {
        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("CaseNumber", "1234123412341234"),
            new OcrDataField("AOSReasonForDivorce", "Adultery"),
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "Proceed"),
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
    public void shouldNotReturnWarningForRespLegalProceedingsDescriptionWhenGivenAndRespLegalProceedingsExist() {
        List<OcrDataField> fieldsUnderTest = asList(
            new OcrDataField("RespLegalProceedingsExist", "Yes"),
            new OcrDataField("RespLegalProceedingsDescription", "My description of my other legal proceedings")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings(), not(hasItem(
            "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No"
            )
        ));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldReturnWarningForRespLegalProceedingsDescriptionWhenNotGivenAndRespLegalProceedingsExist() {
        List<OcrDataField> fieldsUnderTest = singletonList(
            new OcrDataField("RespLegalProceedingsExist", "Yes")
        );

        OcrValidationResult validationResult = classUnderTest.validateBulkScanForm(fieldsUnderTest);

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No"
        ));
    }
}
