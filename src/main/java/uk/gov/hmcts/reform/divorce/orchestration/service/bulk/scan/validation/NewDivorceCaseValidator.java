package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;

@Component
public class NewDivorceCaseValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList("D8PetitionerFirstName", "D8PetitionerLastName");

    protected List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

}