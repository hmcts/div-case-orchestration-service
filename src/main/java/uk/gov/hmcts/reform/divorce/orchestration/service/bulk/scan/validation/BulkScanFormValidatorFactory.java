package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private D8FormValidator d8FormValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(D8_FORM, d8FormValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) throws UnsupportedFormTypeException {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return validators.get(formType);
    }
}
