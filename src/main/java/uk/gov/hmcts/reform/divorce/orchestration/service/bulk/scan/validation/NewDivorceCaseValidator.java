package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.exception.FormFieldValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformFormDateIntoLocalDate;

@Component
public class NewDivorceCaseValidator extends BulkScanFormValidator {

    private static final String EMPTY_PAYMENT_METHOD_ERROR_MESSAGE =
        "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value";
    private static final String MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE =
        "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated";
    private static final String HWF_WRONG_LENGTH_ERROR_MESSAGE =
        "D8HelpWithFeesReferenceNumber is usually 6 digits";

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
        "D8PetitionerPostCode"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("D8LegalProcess", asList("Divorce", "Dissolution", "Judicial (separation)"));
        ALLOWED_VALUES_PER_FIELD.put("D8ScreenHasMarriageCert", asList(TRUE));
        ALLOWED_VALUES_PER_FIELD.put("D8CertificateInEnglish", asList(TRUE, BLANK));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerNameChangedHow", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8PetitionerContactDetailsConfidential", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("D8PaymentMethod", asList("Cheque", "Debit/Credit Card", BLANK));
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
            validatePostcode(fieldsMap, "D8PetitionerPostCode"),
            validateFieldMatchesRegex(fieldsMap, "D8PetitionerEmail", CCD_EMAIL_REGEX),
            validatePayment(fieldsMap)
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

    private static List<String> validatePostcode(Map<String, String> fieldsMap, String postcodeKey) {
        List<String> validationMessages = new ArrayList<>();
        if (fieldsMap.containsKey(postcodeKey)) {
            String postcodeValue = fieldsMap.get(postcodeKey);
            if (isPostcodeLengthValid(postcodeValue)) {
                validationMessages.add(postcodeKey + " is usually 6 or 7 characters long");
            }
        }
        return validationMessages;
    }

    private static boolean isPostcodeLengthValid(String postcode) {
        int postcodeLength = postcode.length();
        return postcodeLength < 6 || postcodeLength > 8;
    }
}