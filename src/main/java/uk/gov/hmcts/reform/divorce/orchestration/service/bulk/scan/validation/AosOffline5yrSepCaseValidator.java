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
public class AosOffline5yrSepCaseValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList(
        "CaseNumber",
        "AOSReasonForDivorce",
        "RespConfirmReadPetition",
        "DateRespReceivedDivorceApplication",
        "RespWillDefendDivorce",
        "RespConsiderFinancialSituation",
        "RespJurisdictionAgree",
        "RespLegalProceedingsExist",
        "RespAgreeToCosts"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("AOSReasonForDivorce", asList("5 years separation"));
        ALLOWED_VALUES_PER_FIELD.put("RespConfirmReadPetition", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespHardshipDefenseResponse", asList(YES_VALUE, NO_VALUE));
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

        if (!isApplicationDateValid(dateRespReceivedDivorceApplication)) {
            validationWarningMessages.add(DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE);
        }

        if (StringUtils.isNotEmpty(respStatementOfTruthSignedDate)) {
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

        return (respJurisdictionAgreeField.equals(YES_VALUE) && StringUtils.isNotEmpty(respJurisdictionDisagreeReasonField));
    }

    private static Boolean didNotAgreeWithRespJurisdictionButDidNotProvideReasonWhyTheyDisagree(String respJurisdictionAgreeField,
                                                                                                String respJurisdictionDisagreeReasonField) {

        return (respJurisdictionAgreeField.equals(NO_VALUE) && StringUtils.isEmpty(respJurisdictionDisagreeReasonField));
    }

    private static List<String> validateRespLegalProceedingsDescription(Map<String, String> fieldsMap) {
        List<String> validationWarningMessages = new ArrayList<>();

        String respLegalProceedingsExistField = fieldsMap.getOrDefault("RespLegalProceedingsExist", "");
        String respLegalProceedingsDescriptionField = fieldsMap.getOrDefault("RespLegalProceedingsDescription", "");

        if (respLegalProceedingsExistField.equals(YES_VALUE) && StringUtils.isEmpty(respLegalProceedingsDescriptionField)) {

            validationWarningMessages.add(RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE);

        } else if (respLegalProceedingsExistField.equals(NO_VALUE) && StringUtils.isNotEmpty(respLegalProceedingsDescriptionField)) {

            validationWarningMessages.add(RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE);
        }

        return validationWarningMessages;
    }
}