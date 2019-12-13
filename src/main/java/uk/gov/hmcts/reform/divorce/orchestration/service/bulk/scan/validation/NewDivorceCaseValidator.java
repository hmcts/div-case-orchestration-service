package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.bsp.common.service.PostcodeValidator.validatePostcode;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformFormDateIntoLocalDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.validateDateComponents;

@Component
public class NewDivorceCaseValidator extends BulkScanFormValidator {

    private static final String EMPTY_PAYMENT_METHOD_ERROR_MESSAGE =
        "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value";
    private static final String MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE =
        "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated";
    private static final String HWF_WRONG_LENGTH_ERROR_MESSAGE =
        "D8HelpWithFeesReferenceNumber is usually 6 digits";
    private static final String EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should not be empty if \"%s\" is \"No\"";
    private static final String NOT_EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should be empty if \"%s\" is \"Yes\"";

    private static final int HELP_WITH_FEES_LENGTH = 6;

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
        "D8MarriedInUk",
        "D8ApplicationToIssueWithoutCertificate",
        "D8MarriageDateDay",
        "D8MarriageDateMonth",
        "D8MarriageDateYear",
        "D8MarriageCertificateCorrect"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("D8LegalProcess", asList("Divorce", "Dissolution", "Judicial (separation)"));
        ALLOWED_VALUES_PER_FIELD.put("D8ScreenHasMarriageCert", asList(TRUE));
        ALLOWED_VALUES_PER_FIELD.put("D8CertificateInEnglish", asList(TRUE, BLANK));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerNameChangedHow", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerContactDetailsConfidential", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8PaymentMethod", asList("Cheque", "Debit/Credit Card", BLANK));
        ALLOWED_VALUES_PER_FIELD.put("PetitionerSolicitor", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerCorrespondenceUseHomeAddress", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8MarriedInUk", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8ApplicationToIssueWithoutCertificate", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8MarriageCertificateCorrect", asList(YES_VALUE, NO_VALUE));
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
            validatePostcode(fieldsMap, "D8PetitionerPostCode"),
            validatePostcode(fieldsMap, "PetitionerSolicitorAddressPostCode"),
            validatePostcode(fieldsMap, "D8PetitionerCorrespondencePostcode"),
            validatePostcode(fieldsMap, "D8ReasonForDivorceAdultery3rdPartyPostCode"),
            validatePayment(fieldsMap),
            validatePlaceOfMarriage(fieldsMap),
            validateMarriageCertificateCorrect(fieldsMap),
            validateDateSplitIntoComponents(fieldsMap, "D8MarriageDateDay", "D8MarriageDateMonth", "D8MarriageDateYear"),
            validateD8PetitionerCorrespondenceAddress(fieldsMap)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());


        try {
            Optional.ofNullable(fieldsMap.get("D8ReasonForDivorceSeparationDate"))
                .ifPresent(formDate -> transformFormDateIntoLocalDate("D8ReasonForDivorceSeparationDate", formDate));
        } catch (FormFieldValidationException exception) {
            errorMessages.add(exception.getMessage());
        }

        return errorMessages;
    }

    private static List<String> validateD8PetitionerCorrespondenceAddress(Map<String, String> fieldsMap) {

        String conditionField = "D8PetitionerCorrespondenceUseHomeAddress";
        List<String> fieldsToCheck = Arrays.asList(
            "D8PetitionerCorrespondenceAddressStreet",
            "D8PetitionerCorrespondenceAddressTown",
            "D8PetitionerCorrespondenceAddressCounty",
            "D8PetitionerCorrespondencePostcode"
        );
        String useHomeAddress = fieldsMap.getOrDefault(conditionField, "");
        if (useHomeAddress.equals(YES_VALUE)) {
            return validateFieldsAreEmpty(fieldsToCheck, conditionField, fieldsMap, NOT_EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE);
        } else if (useHomeAddress.equals((NO_VALUE))) {
            return validateFieldsAreNotEmpty(fieldsToCheck, conditionField, fieldsMap, EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE);
        } else {
            List<String> emptyWarningMessagesList = new ArrayList<>();
            return emptyWarningMessagesList;
        }
    }

    private static List<String> validateFieldsAreEmpty(List<String> fields, String conditionField, Map<String, String> fieldsMap,
                                                       String errorMessage) {
        List<String> validationWarningMessages = new ArrayList<>();
        fields.forEach((fieldKey) -> {
            String fieldValue = fieldsMap.getOrDefault(fieldKey, "");
            if (!StringUtils.isBlank(fieldValue)) {
                validationWarningMessages.add(String.format(errorMessage, fieldKey, conditionField));
            }
        });
        return validationWarningMessages;
    }

    private static List<String> validateFieldsAreNotEmpty(List<String> fields, String conditionField, Map<String, String> fieldsMap,
                                                       String errorMessage) {
        List<String> validationWarningMessages = new ArrayList<>();
        fields.forEach((fieldKey) -> {
            String fieldValue = fieldsMap.getOrDefault(fieldKey, "");
            if (StringUtils.isBlank(fieldValue)) {
                validationWarningMessages.add(String.format(errorMessage, fieldKey, conditionField));
            }
        });
        return validationWarningMessages;
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

        if (d8PaymentMethod.isEmpty() && !isHelpWithFeesValid(hwfReferenceNumber)) {
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
            return Collections.singletonList("Not all date components are present");
        }

        return validateDateComponents(fieldsMap, dayKey, monthKey, yearKey);
    }

}