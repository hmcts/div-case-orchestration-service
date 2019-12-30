package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class AosPack2CaseValidator extends BulkScanFormValidator {

    private static final String DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE =
        "DateRespReceivedDivorceApplication must be a valid 8 digit date";
    private static final String RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE =
        "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No";
    private static final String RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE =
        "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No";
    private static final String EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should not be empty if \"%s\" is \"%s\"";
    private static final String NOT_EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should be empty if \"%s\" is \"%s\"";

    private static final int DATE_RESP_RECEIVED_DIVORCE_APPLICATION_LENGTH = 8;

    private static final List<String> MANDATORY_FIELDS = asList(
        "CaseNumber",
        "AOSReasonForDivorce",
        "RespConfirmReadPetition",
        "DateRespReceivedDivorceApplication",
        "RespAOS2yrConsent",
        "RespWillDefendDivorce",
        "RespConsiderFinancialSituation",
        "RespJurisdictionAgree",
        "RespLegalProceedingsExist",
        "RespAgreeToCosts"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("AOSReasonForDivorce", asList("2 years separation with consent"));
        ALLOWED_VALUES_PER_FIELD.put("RespConfirmReadPetition", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespAOS2yrConsent", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespWillDefendDivorce", asList("Proceed", "Defend"));
        ALLOWED_VALUES_PER_FIELD.put("RespConsiderFinancialSituation", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespJurisdictionAgree", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespLegalProceedingsExist", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespAgreeToCosts", asList(YES_VALUE, NO_VALUE));
        // TODO: verify we get null back from Exela
        ALLOWED_VALUES_PER_FIELD.put("RespStatementOfTruth", asList(YES_VALUE, null));
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
            validateDateRespReceivedDivApplication(fieldsMap),
            validateRespJurisdictionDisagreeReason(fieldsMap),
            validateRespLegalProceedingsDescription(fieldsMap),
            validateRespStatementofTruthSignedDate(fieldsMap)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return errorMessages;
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

    private static List<String> validateDateRespReceivedDivApplication(Map<String, String> fieldsMap) {

        List<String> validationWarningMessages = new ArrayList<>();

        String dateRespReceivedDivorceApplication = fieldsMap.getOrDefault("DateRespReceivedDivorceApplication", "");

        if (!isDateRespReceivedDivApplicationValid(dateRespReceivedDivorceApplication)) {

            validationWarningMessages.add(DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }

    private static boolean isDateRespReceivedDivApplicationValid(String dateRespReceivedDivorceApplication) {
        return (StringUtils.isNumeric(dateRespReceivedDivorceApplication)
            && (dateRespReceivedDivorceApplication.length() == DATE_RESP_RECEIVED_DIVORCE_APPLICATION_LENGTH));
    }

    // TODO: Implement this method
    private static List<String> validateRespJurisdictionDisagreeReason(Map<String, String> fieldsMap) {
        /*
        If RespJurisdictionAgree is 'No',  and RespJurisdictionDisagreeReason is empty, status = warning
        If RespJurisdictionAgree is 'No',  and RespJurisdictionDisagreeReason is not-empty, status = success
        If RespJurisdictionAgree is 'Yes',  and RespJurisdictionDisagreeReason is empty, status = success
        If RespJurisdictionAgree is 'Yes',  and RespJurisdictionDisagreeReason is not-empty, status = warning
        If RespJurisdictionAgree is anything else,  and RespJurisdictionDisagreeReason is empty, status = success
        If RespJurisdictionAgree is anything else,  and RespJurisdictionDisagreeReason is not-empty, status = success
         */

        List<String> validationWarningMessages = new ArrayList<>();

        String respJurisdictionAgreeField = fieldsMap.getOrDefault("RespJurisdictionAgree", "");
        String respJurisdictionDisagreeReasonField = fieldsMap.getOrDefault("RespJurisdictionDisagreeReason", "");

        if (respJurisdictionAgreeField.equals(YES_VALUE) && !respJurisdictionDisagreeReasonField.isEmpty()) {

            validationWarningMessages.add(RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE);

        } else if (respJurisdictionAgreeField.equals(NO_VALUE) && respJurisdictionDisagreeReasonField.isEmpty()) {

            validationWarningMessages.add(RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }

    // TODO: Implement this method
    private static List<String> validateRespLegalProceedingsDescription(Map<String, String> fieldsMap) {
        /*
        If RespLegalProceedingsExist is 'Yes', and  RespLegalProceedingsDescription is empty, status = warning
        If RespLegalProceedingsExist is 'Yes', and  RespLegalProceedingsDescription is not-empty, status = success
        If RespLegalProceedingsExist is 'No', and  RespLegalProceedingsDescription is empty, status = success
        If RespLegalProceedingsExist is 'No', and  RespLegalProceedingsDescription is not-empty, status = warning
        If RespLegalProceedingsExist is anything else,  and RespLegalProceedingsDescription is empty, status = success
        If RespLegalProceedingsExist is anything else,  and RespLegalProceedingsDescription is not-empty, status = success
         */

        List<String> validationWarningMessages = new ArrayList<>();

        String respLegalProceedingsExistField = fieldsMap.getOrDefault("RespLegalProceedingsExist", "");
        String respLegalProceedingsDescriptionField = fieldsMap.getOrDefault("RespLegalProceedingsDescription", "");

        if (respLegalProceedingsExistField.equals(YES_VALUE) && respLegalProceedingsDescriptionField.isEmpty()) {

            validationWarningMessages.add(RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE);

        } else if (respLegalProceedingsExistField.equals(NO_VALUE) && !respLegalProceedingsDescriptionField.isEmpty()) {

            validationWarningMessages.add(RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }

    // TODO: Implement this method
    private static List<String> validateRespStatementofTruthSignedDate(Map<String, String> fieldsMap) {
        List<String> validationMessages = new ArrayList<>();

        return validationMessages;
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
}