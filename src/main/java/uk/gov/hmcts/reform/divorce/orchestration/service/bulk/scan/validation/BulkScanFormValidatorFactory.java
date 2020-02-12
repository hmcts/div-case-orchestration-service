package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_2_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_5_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_ADULTERY_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_BEHAVIOUR_DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private D8FormValidator d8FormValidator;

    @Autowired
    private AosOffline2yrSepCaseValidator aosOffline2yrSepCaseValidator;

    @Autowired
    private AosOffline5yrSepCaseValidator aosOffline5yrSepCaseValidator;

    @Autowired
    private AosOfflineBehaviourDesertionCaseValidator aosOfflineBehaviourDesertionCaseValidator;

    @Autowired
    private AosOfflineAdulteryCoRespCaseValidator aosOfflineAdulteryCoRespCaseValidator;

    @Autowired
    private AosOfflineAdulteryCaseValidator aosOfflineAdulteryCaseValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(D8_FORM, d8FormValidator);
        validators.put(AOS_OFFLINE_2_YR_SEP, aosOffline2yrSepCaseValidator);
        validators.put(AOS_OFFLINE_5_YR_SEP, aosOffline5yrSepCaseValidator);
        validators.put(AOS_OFFLINE_BEHAVIOUR_DESERTION, aosOfflineBehaviourDesertionCaseValidator);
        validators.put(AOS_OFFLINE_ADULTERY_CO_RESP, aosOfflineAdulteryCoRespCaseValidator);
        validators.put(AOS_OFFLINE_ADULTERY, aosOfflineAdulteryCaseValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) throws UnsupportedFormTypeException {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return validators.get(formType);
    }
}
