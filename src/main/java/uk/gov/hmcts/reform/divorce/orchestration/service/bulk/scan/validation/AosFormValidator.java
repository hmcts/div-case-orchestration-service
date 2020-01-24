package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public abstract class AosFormValidator extends BulkScanFormValidator {

    private static final String DATE_RESP_RECEIVED_DIV_APP_WRONG_LENGTH_ERROR_MESSAGE =
        "DateRespReceivedDivorceApplication must be a valid 8 digit date";
    private static final String RESP_STATEMENT_OF_TRUTH_WRONG_LENGTH_ERROR_MESSAGE =
        "RespStatementofTruthSignedDate must be a valid 8 digit date";
    private static final String RESP_JURISDICTION_DISAGREE_REASON_ERROR_MESSAGE =
        "RespJurisdictionDisagreeReason must not be empty if 'RespJurisdictionAgree' is 'No";
    private static final String RESP_LEGAL_PROCEEDINGS_ERROR_MESSAGE =
        "RespLegalProceedingsDescription must not be empty if 'RespLegalProceedingsExist' is 'No";

    private static final List<String> COMMON_MANDATORY_FIELDS_FOR_ALL_AOS_FORMS = asList(
        "CaseNumber",
        "AOSReasonForDivorce",
        "RespConfirmReadPetition",
        "DateRespReceivedDivorceApplication",
        "RespWillDefendDivorce",
        "RespJurisdictionAgree",
        "RespLegalProceedingsExist",
        "RespAgreeToCosts"
    );

    protected List<String> getMandatoryFields() {
        List<String> allMandatoryFields = new ArrayList<>(COMMON_MANDATORY_FIELDS_FOR_ALL_AOS_FORMS);
        allMandatoryFields.addAll(getAosOfflineSpecificMandatoryFields());
        return allMandatoryFields.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    protected abstract List<String> getAosOfflineSpecificMandatoryFields();

    protected abstract List<String> getAosFormSpecificFieldValidation(Map<String, String> fieldsMap);

    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        return Stream.of(
            validateDatesRelevantToAosApplication(fieldsMap),
            validateRespJurisdictionDisagreeReason(fieldsMap),
            validateRespLegalProceedingsDescription(fieldsMap),
            getAosFormSpecificFieldValidation(fieldsMap)
        ).flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<String> validateRespJurisdictionDisagreeReason(Map<String, String> fieldsMap) {
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

    private Boolean didAgreeRespJurisdictionButProvidedReasonWhyTheyDisagree(String respJurisdictionAgreeField,
                                                                             String respJurisdictionDisagreeReasonField) {
        return (respJurisdictionAgreeField.equals(YES_VALUE) && StringUtils.isNotEmpty(respJurisdictionDisagreeReasonField));
    }

    private Boolean didNotAgreeWithRespJurisdictionButDidNotProvideReasonWhyTheyDisagree(String respJurisdictionAgreeField,
                                                                                         String respJurisdictionDisagreeReasonField) {
        return (respJurisdictionAgreeField.equals(NO_VALUE) && StringUtils.isEmpty(respJurisdictionDisagreeReasonField));
    }

    private final List<String> validateRespLegalProceedingsDescription(Map<String, String> fieldsMap) {
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

    private List<String> validateDatesRelevantToAosApplication(Map<String, String> fieldsMap) {
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

    private boolean isApplicationDateValid(String date) {
        return (StringUtils.isNumeric(date)
            && (date.length() == AOS_FORM_DATE_LENGTH));
    }
}
