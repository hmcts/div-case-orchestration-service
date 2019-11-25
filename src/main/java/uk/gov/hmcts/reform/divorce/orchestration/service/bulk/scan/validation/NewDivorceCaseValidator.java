package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Component
public class NewDivorceCaseValidator extends BulkScanFormValidator {

    private static String EMPTY_PAYMENT_METHOD_ERROR_MESSAGE =
            "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value";
    private static String MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE =
            "D8PaymentMethod and D8HelpWithFeesReferenceNumber should not both be populated";
    private static String HWF_WRONG_LENGTH_ERROR_MESSAGE =
            "D8HelpWithFeesReferenceNumber is usually 6 digits";

    private static List<String> VALIDATION_ERROR_MESSAGES = new ArrayList<>();

    private static final List<String> MANDATORY_FIELDS = asList(
        "D8PetitionerFirstName",
        "D8PetitionerLastName",
        "D8LegalProcess",
        "D8ScreenHasMarriageCert",
        "D8RespondentFirstName",
        "D8RespondentLastName"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("D8LegalProcess", asList("Divorce", "Dissolution", "Judicial (separation)"));
        ALLOWED_VALUES_PER_FIELD.put("D8ScreenHasMarriageCert", asList(TRUE));
        ALLOWED_VALUES_PER_FIELD.put("D8CertificateInEnglish", asList(TRUE, BLANK));
        //ALLOWED_VALUES_PER_FIELD.put("D8PaymentMethod", asList("cheque", "card", BLANK));
        // add here phone number regex
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
        //validatePayment(fieldsMap);

        return VALIDATION_ERROR_MESSAGES;
    }

    private static void validatePayment(Map<String, String> fieldsMap) {
        String hwfReferenceNumber = fieldsMap.get("D8HelpWithFeesReferenceNumber");
        String d8PaymentMethod = fieldsMap.get("D8PaymentMethod");

        boolean onlySinglePaymentMethodProvided =
                ((StringUtils.isNotEmpty(hwfReferenceNumber) && StringUtils.isEmpty(d8PaymentMethod))
                        || (StringUtils.isEmpty(hwfReferenceNumber) && StringUtils.isNotEmpty(d8PaymentMethod)));

        boolean noPaymentMethodProvided = StringUtils.isNotEmpty(hwfReferenceNumber)
                && StringUtils.isNotEmpty(d8PaymentMethod);

        if (hwfReferenceNumber.length() !=  6) {
            // HWF length should only be 6 digits
            VALIDATION_ERROR_MESSAGES.add(HWF_WRONG_LENGTH_ERROR_MESSAGE);
        }

        if (noPaymentMethodProvided) {
            // if neither exist then fail
            VALIDATION_ERROR_MESSAGES.add(EMPTY_PAYMENT_METHOD_ERROR_MESSAGE);
        }

        if (!onlySinglePaymentMethodProvided) {
            // if multiple payment methods exist then fail
            VALIDATION_ERROR_MESSAGES.add(MULTIPLE_PAYMENT_METHODS_ERROR_MESSAGE);
        }
    }
}