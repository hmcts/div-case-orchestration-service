package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static java.lang.String.format;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private NewDivorceCaseValidator newDivorceCaseValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(BulkScanForms.NEW_DIVORCE_CASE, newDivorceCaseValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedOperationException(format("\"%s\" form type is not supported", formType));
        }

        return validators.get(formType);
    }

}