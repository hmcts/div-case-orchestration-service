package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_2_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_5_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;

<<<<<<<HEAD
=======
    >>>>>>>master

@Component
public class BulkScanFormTransformerFactory {

    @Autowired
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @Autowired
    private AosOffline2YrSepFormToCaseTransformer aosOffline2YrSepFormToCaseTransformer;

    @Autowired
    private AosOffline5YrSepFormToCaseTransformer aosOffline5YrSepFormToCaseTransformer;

    private static Map<String, BulkScanFormTransformer> bulkScanFormTransformerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap.put(D8_FORM, d8FormToCaseTransformer);
        bulkScanFormTransformerMap.put(AOS_OFFLINE_2_YR_SEP, aosOffline2YrSepFormToCaseTransformer);
        bulkScanFormTransformerMap.put(AOS_OFFLINE_5_YR_SEP, aosOffline5YrSepFormToCaseTransformer);
    }

    public BulkScanFormTransformer getTransformer(String formType) {
        if (!bulkScanFormTransformerMap.containsKey(formType)) {
            throw new UnsupportedFormTypeException(format("Form type \"%s\" is not supported.", formType));
        }

        return bulkScanFormTransformerMap.get(formType);
    }
}