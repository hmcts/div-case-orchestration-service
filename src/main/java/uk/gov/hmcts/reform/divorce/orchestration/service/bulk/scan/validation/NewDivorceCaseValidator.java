package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Component
public class NewDivorceCaseValidator extends BulkScanFormValidator {

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
        // add here phone number regex
    }

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

}