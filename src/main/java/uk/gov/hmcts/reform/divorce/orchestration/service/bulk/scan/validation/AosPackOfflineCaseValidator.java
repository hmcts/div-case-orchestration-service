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
public class AosPackOfflineCaseValidator extends BulkScanFormValidator {

    private static final String DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE =
        "DateRespReceivedDivorceApplication must be a valid 8 digit date";
    private static final String RESP_STATEMENT_OF_TRUTH_WRONG_LENGTH_ERROR_MESSAGE =
        "RespStatementofTruthSignedDate must be a valid 8 digit date";
    private static final String RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE =
        "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No";
    private static final String RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE =
        "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No";

    private static final int AOS_FORM_DATE_LENGTH = 8;

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
            validateDatesRelevantToAosApplication(fieldsMap),
            validateRespJurisdictionDisagreeReason(fieldsMap),
            validateRespLegalProceedingsDescription(fieldsMap)
        ).flatMap(Collection::stream)
            .collect(Collectors.toList());

        return errorMessages;
    }

    private static List<String> validateDatesRelevantToAosApplication(Map<String, String> fieldsMap) {

        List<String> validationWarningMessages = new ArrayList<>();

        String dateRespReceivedDivorceApplication = fieldsMap.getOrDefault("DateRespReceivedDivorceApplication", "");
        String respStatementOfTruthSignedDate = fieldsMap.getOrDefault("RespStatementofTruthSignedDate", "");

        // DateRespReceivedDivorceApplication is always validated as it is a required field
        if (!isApplicationDateValid(dateRespReceivedDivorceApplication)) {
            validationWarningMessages.add(DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE);
        }

        // RespStatementofTruthSignedDate should only be validated if it is present
        if (!respStatementOfTruthSignedDate.isEmpty()) {
            if (!isApplicationDateValid(respStatementOfTruthSignedDate)) {
                validationWarningMessages.add(RESP_STATEMENT_OF_TRUTH_WRONG_LENGTH_ERROR_MESSAGE);
            }
        }

        return validationWarningMessages;
    }

    private static boolean isApplicationDateValid(String date) {
        return (StringUtils.isNumeric(date)
            && (date.length() == AOS_FORM_DATE_LENGTH));
    }

    private static List<String> validateRespJurisdictionDisagreeReason(Map<String, String> fieldsMap) {

        List<String> validationWarningMessages = new ArrayList<>();

        String respJurisdictionAgreeField = fieldsMap.getOrDefault("RespJurisdictionAgree", "");
        String respJurisdictionDisagreeReasonField = fieldsMap.getOrDefault("RespJurisdictionDisagreeReason", "");

        if (didAgreeRespJurisdictionButProvidedReasonWhyTheyDisagree(respJurisdictionAgreeField, respJurisdictionDisagreeReasonField)) {
            validationWarningMessages.add(RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE);
        }

        if (didNotAgreeWithRespJurisdictionButDidNotProvideReasonWhyTheyDisagree(respJurisdictionAgreeField, respJurisdictionDisagreeReasonField)) {
            validationWarningMessages.add(RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }

    private static Boolean didAgreeRespJurisdictionButProvidedReasonWhyTheyDisagree(String respJurisdictionAgreeField,
                                                                                    String respJurisdictionDisagreeReasonField) {

        return (respJurisdictionAgreeField.equals(YES_VALUE) && !respJurisdictionDisagreeReasonField.isEmpty());
    }

    private static Boolean didNotAgreeWithRespJurisdictionButDidNotProvideReasonWhyTheyDisagree(String respJurisdictionAgreeField,
                                                                                                String respJurisdictionDisagreeReasonField) {

        return (respJurisdictionAgreeField.equals(NO_VALUE) && respJurisdictionDisagreeReasonField.isEmpty());
    }

    private static List<String> validateRespLegalProceedingsDescription(Map<String, String> fieldsMap) {

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
}