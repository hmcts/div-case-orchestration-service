package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class D8FormToCaseTransformer implements ExceptionRecordToCaseTransformer {

    private static Map<String, String> ocrToCCDMapping;

    public D8FormToCaseTransformer() {
        ocrToCCDMapping = d8ExceptionRecordToCcdMap();
    }

    private static Map<String, String> d8ExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("firstName", "D8FirstName");
        erToCcdFieldsMap.put("lastName", "D8LastName");

        return erToCcdFieldsMap;
    }

    @Override
    public Map<String, Object> toCase(ExceptionRecord exceptionRecord) {
        return exceptionRecord.getOcrDataFields().stream()
            .collect(Collectors.toMap(
                ocrDataField -> ocrToCCDMapping.get(ocrDataField.getName()),
                OcrDataField::getValue
            ));
    }
}