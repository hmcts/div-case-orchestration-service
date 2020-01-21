package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.bsp.common.service.PostcodeValidator.validatePostcode;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformFormDateIntoLocalDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.validateDateComponents;

@Component
public class D8FormValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList(
        "D8PetitionerFirstName",
        "D8PetitionerLastName",
        "D8PetitionerNameChangedHow",
        "D8LegalProcess",
        "D8ScreenHasMarriageCert",
        "D8RespondentFirstName",
        "D8RespondentLastName",
        "D8MarriagePetitionerName",
        "D8MarriageRespondentName",
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
        "D8MarriageCertificateCorrect",
        "D8FinancialOrder",
        "D8ReasonForDivorce",
        "D8LegalProceedings",
        "D8AppliesForStatementOfTruth",
        "D8DivorceClaimFrom",
        "D8FullNameStatementOfTruth",
        "D8StatementofTruthSignature",
        "D8StatementofTruthDate",
        "D8SolicitorsFirmStatementOfTruth"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        List<String> yesNoValues = asList(YES_VALUE, NO_VALUE);
        ALLOWED_VALUES_PER_FIELD.put("D8LegalProcess", asList("Divorce", "Dissolution", "Judicial (separation)"));
        ALLOWED_VALUES_PER_FIELD.put("D8ScreenHasMarriageCert", asList(TRUE));
        ALLOWED_VALUES_PER_FIELD.put("D8CertificateInEnglish", asList(TRUE, EMPTY_STRING));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerNameChangedHow", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerContactDetailsConfidential", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8PaymentMethod", asList("Cheque", "Debit/Credit Card", EMPTY_STRING));
        ALLOWED_VALUES_PER_FIELD.put("PetitionerSolicitor", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerCorrespondenceUseHomeAddress", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerNameDifferentToMarriageCert", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8RespondentCorrespondenceSendToSol", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8MarriedInUk", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8ApplicationToIssueWithoutCertificate", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8MarriageCertificateCorrect", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8FinancialOrder", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8FinancialOrderFor", asList("myself", "my children", "myself, my children", EMPTY_STRING));
        ALLOWED_VALUES_PER_FIELD.put("D8ReasonForDivorce", asList("unreasonable-behaviour", "adultery", "desertion", "separation-2-years",
            "separation-5-years"));
        ALLOWED_VALUES_PER_FIELD.put("D8LegalProceedings", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("D8AppliesForStatementOfTruth", asList("marriage", "dissolution", "separation"));
        ALLOWED_VALUES_PER_FIELD.put("D8DivorceClaimFrom", asList("respondent", "corespondent", "respondent, corespondent"));
        ALLOWED_VALUES_PER_FIELD.put("D8StatementofTruthSignature", asList(YES_VALUE));
    }

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        List<String> errorMessages = Stream.of(
            validateFieldMatchesRegex(fieldsMap, "D8PetitionerPhoneNumber", CCD_PHONE_NUMBER_REGEX),
            validateFieldMatchesRegex(fieldsMap, "D8RespondentPhoneNumber", CCD_PHONE_NUMBER_REGEX),
            validateFieldMatchesRegex(fieldsMap, "PetitionerSolicitorPhone", CCD_PHONE_NUMBER_REGEX),
            validateFieldMatchesRegex(fieldsMap, "D8PetitionerEmail", CCD_EMAIL_REGEX),
            validateFieldMatchesRegex(fieldsMap, "PetitionerSolicitorEmail", CCD_EMAIL_REGEX),
            validateFieldMatchesRegex(fieldsMap, "D8RespondentEmailAddress", CCD_EMAIL_REGEX),
            validatePostcode(fieldsMap, "D8PetitionerPostCode"),
            validatePostcode(fieldsMap, "PetitionerSolicitorAddressPostCode"),
            validatePostcode(fieldsMap, "D8PetitionerCorrespondencePostcode"),
            validatePostcode(fieldsMap, "D8ReasonForDivorceAdultery3rdPartyPostCode"),
            validatePostcode(fieldsMap, "D8RespondentPostcode"),
            validatePostcode(fieldsMap, "D8RespondentSolicitorAddressPostCode"),
            validatePayment(fieldsMap),
            validatePlaceOfMarriage(fieldsMap),
            validateMarriageCertificateCorrect(fieldsMap),
            validateDateSplitIntoComponents(fieldsMap, "D8MarriageDateDay", "D8MarriageDateMonth", "D8MarriageDateYear"),
            validateD8PetitionerCorrespondenceAddress(fieldsMap),
            validateD8FinancialOrderFor(fieldsMap)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        validateDateFromGivenField(fieldsMap, "D8ReasonForDivorceSeparationDate").ifPresent(errorMessages::add);
        validateDateFromGivenField(fieldsMap, "D8StatementofTruthDate").ifPresent(errorMessages::add);

        return errorMessages;
    }

    /**
     * Validates that value from given field is a valid date and returns an optional validation message.
     */
    private Optional<String> validateDateFromGivenField(Map<String, String> fieldsMap, String fieldName) {
        Optional<String> validationMessage = Optional.empty();

        try {
            Optional.ofNullable(fieldsMap.get(fieldName))
                .ifPresent(formDate -> transformFormDateIntoLocalDate(fieldName, formDate));
        } catch (FormFieldValidationException exception) {
            validationMessage = Optional.of(exception.getMessage());
        }

        return validationMessage;
    }

    private static List<String> validateD8FinancialOrderFor(Map<String, String> fieldsMap) {
        List<String> validationMessages =
            validateFieldsAreNotEmptyOnlyFor(YES_VALUE, "D8FinancialOrder", singletonList("D8FinancialOrderFor"), fieldsMap);

        String financialOrderFor = fieldsMap.getOrDefault("D8FinancialOrderFor", "")
            .replace("myself", "petitioner")
            .replace("my children", "children");
        String financialOrderStatementOfTruth = fieldsMap.getOrDefault("D8FinancialOrderStatementOfTruth", "");
        if (!financialOrderFor.equals(financialOrderStatementOfTruth)) {
            validationMessages.add("Fields selected for \"D8FinancialOrderStatementOfTruth\" need to be consistent with \"D8FinancialOrderFor\"");
        }

        return validationMessages;
    }

    private static List<String> validateD8PetitionerCorrespondenceAddress(Map<String, String> fieldsMap) {
        List<String> fieldsToCheck = Arrays.asList(
            "D8PetitionerCorrespondenceAddressStreet",
            "D8PetitionerCorrespondenceAddressTown",
            "D8PetitionerCorrespondenceAddressCounty",
            "D8PetitionerCorrespondencePostcode"
        );
        return validateFieldsAreNotEmptyOnlyFor(NO_VALUE, "D8PetitionerCorrespondenceUseHomeAddress", fieldsToCheck, fieldsMap);
    }

    private static List<String> validateFieldsAreNotEmptyOnlyFor(String valueForWhichFieldsShouldNotBeEmpty, String conditionField,
                                                                 List<String> fieldsToCheck, Map<String, String> fieldsMap) {
        String conditionFieldValue = fieldsMap.getOrDefault(conditionField, "");

        if (conditionFieldValue.equals(valueForWhichFieldsShouldNotBeEmpty)) {
            return validateFieldsAreNotEmpty(fieldsToCheck, conditionField, fieldsMap,
                EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE, conditionFieldValue);
        } else {
            return validateFieldsAreEmpty(fieldsToCheck, conditionField, fieldsMap,
                NOT_EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE, conditionFieldValue);
        }
    }

    private static List<String> validateFieldsAreEmptyIs(boolean expected, List<String> fields, String conditionField,
                                                         Map<String, String> fieldsMap, String errorMessage, String conditionFieldValue) {
        List<String> validationWarningMessages = new ArrayList<>();
        fields.forEach((fieldKey) -> {
            String fieldValue = fieldsMap.getOrDefault(fieldKey, "");
            boolean fieldValueIsAsItShould = StringUtils.isBlank(fieldValue) == expected;
            if (!fieldValueIsAsItShould) {
                validationWarningMessages.add(String.format(errorMessage, fieldKey, conditionField, conditionFieldValue));
            }
        });
        return validationWarningMessages;
    }

    private static List<String> validateFieldsAreEmpty(List<String> fields, String conditionField, Map<String, String> fieldsMap,
                                                       String errorMessage, String conditionFieldValue) {
        return validateFieldsAreEmptyIs(true, fields, conditionField, fieldsMap, errorMessage, conditionFieldValue);
    }

    private static List<String> validateFieldsAreNotEmpty(List<String> fields, String conditionField, Map<String, String> fieldsMap,
                                                          String errorMessage, String conditionFieldValue) {
        return validateFieldsAreEmptyIs(false, fields, conditionField, fieldsMap, errorMessage, conditionFieldValue);
    }

    private static List<String> validatePayment(Map<String, String> fieldsMap) {

        List<String> validationWarningMessages = new ArrayList<>();

        String hwfReferenceNumber = fieldsMap.getOrDefault("D8HelpWithFeesReferenceNumber", "");
        String d8PaymentMethod = fieldsMap.getOrDefault("D8PaymentMethod", "");

        boolean isMultiplePaymentMethodsProvided =
            ((StringUtils.isNotEmpty(hwfReferenceNumber) && StringUtils.isNotEmpty(d8PaymentMethod)));

        boolean isNoPaymentMethodProvided = StringUtils.isEmpty(hwfReferenceNumber)
            && StringUtils.isEmpty(d8PaymentMethod);

        if (isNoPaymentMethodProvided) {
            validationWarningMessages.add(EMPTY_PAYMENT_METHOD_ERROR_MESSAGE);
        }

        if (isMultiplePaymentMethodsProvided) {
            validationWarningMessages.add(MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE);
        }

        if (StringUtils.isEmpty(d8PaymentMethod) && !isHelpWithFeesValid(hwfReferenceNumber)) {
            validationWarningMessages.add(HWF_WRONG_LENGTH_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }

    private static boolean isHelpWithFeesValid(String hwfReferenceNumber) {
        return (StringUtils.isNumeric(hwfReferenceNumber) && (hwfReferenceNumber.length() == HELP_WITH_FEES_LENGTH));
    }

    private static List<String> validateFieldMatchesRegex(Map<String, String> fieldsMap, String fieldKey, String validationRegex) {
        List<String> validationMessages = new ArrayList<>();

        if (fieldsMap.containsKey(fieldKey)) {
            String valueToValidate = fieldsMap.get(fieldKey);
            if (!valueToValidate.matches(validationRegex)) {
                validationMessages.add(fieldKey + " is not in a valid format");
            }
        }
        return validationMessages;
    }

    private static List<String> validatePlaceOfMarriage(Map<String, String> fieldsMap) {
        List<String> validationWarningMessages = new ArrayList<>();

        String marriedInTheUk = fieldsMap.getOrDefault("D8MarriedInUk", "");
        String d8ApplicationToIssueWithoutCertificate = fieldsMap.getOrDefault("D8ApplicationToIssueWithoutCertificate", "");
        String d8MarriagePlaceOfMarriage = fieldsMap.getOrDefault("D8MarriagePlaceOfMarriage", "");

        boolean bothMandatoryPresent =
            Stream.of(marriedInTheUk, d8ApplicationToIssueWithoutCertificate)
                .map(StringUtils::isNotEmpty)
                .reduce(Boolean::logicalAnd)
                .orElse(false);

        if (bothMandatoryPresent) {
            if (StringUtils.isEmpty(d8MarriagePlaceOfMarriage)) {
                validationWarningMessages.add(
                    "\"D8MarriagePlaceOfMarriage\" can't be empty for any values of \"D8MarriedInUk\" and "
                        + "\"D8ApplicationToIssueWithoutCertificate\"");
            }
        }

        return validationWarningMessages;
    }

    private static List<String> validateMarriageCertificateCorrect(Map<String, String> fieldsMap) {
        List<String> validationWarningMessages = new ArrayList<>();

        String d8MarriageCertificateCorrectExplain = fieldsMap.getOrDefault("D8MarriageCertificateCorrectExplain", "");

        if (fieldsMap.containsKey("D8MarriageCertificateCorrect")) {
            String d8MarriageCertificateCorrect = fieldsMap.get("D8MarriageCertificateCorrect");
            if (d8MarriageCertificateCorrect.equals(NO_VALUE) && StringUtils.isEmpty(d8MarriageCertificateCorrectExplain)) {
                validationWarningMessages.add(
                    "If D8MarriageCertificateCorrect is \"No\", then D8MarriageCertificateCorrectExplain should not be empty");
            }
            if (d8MarriageCertificateCorrect.equals(YES_VALUE) && StringUtils.isNotEmpty(d8MarriageCertificateCorrectExplain)) {
                validationWarningMessages.add(
                    "If D8MarriageCertificateCorrect is \"Yes\", then D8MarriageCertificateCorrectExplain should be empty");
            }
        }
        return validationWarningMessages;
    }

    private static List<String> validateDateSplitIntoComponents(Map<String, String> fieldsMap, String dayKey, String monthKey, String yearKey) {
        boolean allDateComponentsPresent = Stream.of(dayKey, monthKey, yearKey)
            .map(fieldsMap::containsKey)
            .reduce(Boolean::logicalAnd)
            .orElse(false);

        if (!allDateComponentsPresent) {
            return singletonList("Not all date components are present");
        }

        return validateDateComponents(fieldsMap, dayKey, monthKey, yearKey);
    }
}
