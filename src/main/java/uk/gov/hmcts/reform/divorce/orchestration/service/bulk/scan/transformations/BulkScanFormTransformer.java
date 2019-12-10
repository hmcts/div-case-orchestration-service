package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BulkScanFormTransformer {

    private static final String BULK_SCAN_CASE_REFERENCE = "bulkScanCaseReference";

    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {
        List<OcrDataField> ocrDataFields = exceptionRecord.getOcrDataFields();

        Map<String, Object> caseData = mapOcrFieldsToCaseData(ocrDataFields);

        // Need to store the Exception Record ID as part of the CCD data
        caseData.put(BULK_SCAN_CASE_REFERENCE, exceptionRecord.getId());

        Map<String, Object> formSpecificMap = runFormSpecificTransformation(ocrDataFields);
        caseData.putAll(formSpecificMap);

        caseData = runPostMappingModification(caseData);

        return caseData;
    }

    private Map<String, Object> mapOcrFieldsToCaseData(List<OcrDataField> ocrDataFields) {
        Map<String, String> ocrToCCDMapping = getOcrToCCDMapping();

        return ocrDataFields.stream()
            .filter(ocrDataField -> ocrToCCDMapping.containsKey(ocrDataField.getName()))
            .collect(Collectors.toMap(
                ocrDataField -> ocrToCCDMapping.get(ocrDataField.getName()), OcrDataField::getValue
            ));
    }

    abstract Map<String, String> getOcrToCCDMapping();

    abstract Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields);

    abstract Map<String, Object> runPostMappingModification(Map<String, Object> ccdTransformedFields);

}
