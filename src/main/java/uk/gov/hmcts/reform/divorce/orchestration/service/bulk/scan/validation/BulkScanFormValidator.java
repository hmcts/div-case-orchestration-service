package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class BulkScanFormValidator {

    public OcrValidationResult validateBulkScanForm(List<OcrDataField> ocrDataFields) {
        Map<String, String> filledFormFields = getFilledFormFields(ocrDataFields);

        List<String> errors = validateMandatoryFieldsArePresent(filledFormFields);

        return new OcrValidationResult(emptyList(), errors);
    }

    private Map<String, String> getFilledFormFields(List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

    private List<String> validateMandatoryFieldsArePresent(Map<String, String> filledFormFields) {
        return getMandatoryFields().stream()
            .filter(f -> !filledFormFields.containsKey(f))
            .map(f -> String.format("Mandatory field \"%s\" is missing", f))
            .collect(Collectors.toList());
    }

    protected abstract List<String> getMandatoryFields();

}