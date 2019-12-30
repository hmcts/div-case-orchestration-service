package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_PACK_2;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.NEW_DIVORCE_CASE;

@Component
public class BulkScanFormTransformerFactory {

    @Autowired
    private D8FormToCaseTransformer d8FormToCaseTransformer;
    private AosPack2FormToCaseTransformer aosPack2FormToCaseTransformer;

    private static Map<String, BulkScanFormTransformer> bulkScanFormTransformerMap = new HashMap<>();

    @PostConstruct
    public void init() {

        bulkScanFormTransformerMap.put(NEW_DIVORCE_CASE, d8FormToCaseTransformer);
        bulkScanFormTransformerMap.put(AOS_PACK_2, aosPack2FormToCaseTransformer);
    }

    public BulkScanFormTransformer getTransformer(String formType) {
        if (!bulkScanFormTransformerMap.containsKey(formType)) {
            throw new UnsupportedFormTypeException(format("Form type \"%s\" is not supported.", formType));
        }

        return bulkScanFormTransformerMap.get(formType);
    }

}