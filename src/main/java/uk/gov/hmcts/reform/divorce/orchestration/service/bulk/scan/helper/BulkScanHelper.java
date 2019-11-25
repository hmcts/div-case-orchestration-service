package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkScanHelper {

    /**
     * Returns map with only the fields that were not blank from the OCR data.
     */
    public static Map<String, String> produceMapWithoutEmptyEntries(List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

}