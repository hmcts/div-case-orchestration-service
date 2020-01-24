package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.produceMapWithoutEmptyEntries;

public abstract class BulkScanFormValidator {

    static final String EMPTY_PAYMENT_METHOD_ERROR_MESSAGE =
        "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value";
    static final String MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE =
        "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated";
    static final String HWF_WRONG_LENGTH_ERROR_MESSAGE =
        "D8HelpWithFeesReferenceNumber is usually 6 digits";
    static final String EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should not be empty if \"%s\" is \"%s\"";
    static final String NOT_EMPTY_CONDITIONAL_MANDATORY_FIELD_ERROR_MESSAGE =
        "\"%s\" should be empty if \"%s\" is \"%s\"";


    static final int AOS_FORM_DATE_LENGTH = 8;
    static final int HELP_WITH_FEES_LENGTH = 6;
    static final String TRUE = "True";

    protected abstract List<String> getMandatoryFields();

    protected abstract List<String> runPostProcessingValidation(Map<String, String> fieldsMap);

    public OcrValidationResult validateBulkScanForm(List<OcrDataField> ocrDataFields) {
        OcrValidationResult.Builder validationResultBuilder = OcrValidationResult.builder();

        Map<String, String> fieldsMap = produceMapWithoutEmptyEntries(ocrDataFields);

        List<String> validationMessagesForMissingMandatory = produceErrorsForMissingMandatoryFields(fieldsMap);
        validationMessagesForMissingMandatory.forEach(validationResultBuilder::addWarning);

        List<String> validationMessagesForValuesNotAllowed = produceErrorsForValuesNotAllowed(fieldsMap);
        validationMessagesForValuesNotAllowed.forEach(validationResultBuilder::addWarning);

        List<String> validationMessagesFromPostProcessingValidation = runPostProcessingValidation(fieldsMap);
        validationMessagesFromPostProcessingValidation.forEach(validationResultBuilder::addWarning);

        return validationResultBuilder.build();
    }

    private List<String> produceErrorsForMissingMandatoryFields(Map<String, String> fieldsMap) {
        return getMandatoryFields().stream()
            .filter(f -> !fieldsMap.containsKey(f))
            .map(f -> String.format("Mandatory field \"%s\" is missing", f))
            .collect(Collectors.toList());
    }

    private List<String> produceErrorsForValuesNotAllowed(Map<String, String> fieldsMap) {
        List<String> validationErrorMessages = new ArrayList<>();

        getAllowedValuesPerField().forEach((fieldName, allowedValues) -> {
            if (fieldsMap.containsKey(fieldName)) {
                String ocrFieldValue = fieldsMap.get(fieldName);
                if (!allowedValues.contains(ocrFieldValue)) {
                    String errorMessage = produceErrorMessageForValueNotAllowed(fieldName, allowedValues);
                    validationErrorMessages.add(errorMessage);
                }
            }
        });

        return validationErrorMessages;
    }

    protected abstract Map<String, List<String>> getAllowedValuesPerField();

    private String produceErrorMessageForValueNotAllowed(String fieldName, List<String> allowedValues) {
        StringBuilder errorMessage = new StringBuilder();

        int arraySize = allowedValues.size();
        for (int i = 1; i <= arraySize; i++) {
            String allowedValue = allowedValues.get(i - 1);

            if (StringUtils.isNotBlank(allowedValue)) {
                errorMessage.append("\"");
                errorMessage.append(allowedValue);
                errorMessage.append("\"");
            } else {
                errorMessage.append("left blank");
            }

            boolean lastItem = i == arraySize;
            if (!lastItem) {
                boolean itemBeforeLast = i == arraySize - 1;
                if (!itemBeforeLast) {
                    errorMessage.append(", ");
                } else {
                    errorMessage.append(" or ");
                }
            }
        }

        return format("%s must be %s", fieldName, errorMessage.toString());
    }
}