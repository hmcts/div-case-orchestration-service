package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out;

import lombok.Getter;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Getter
public class OcrValidationResult {

    private final List<String> warnings;
    private final List<String> errors;
    private final ValidationStatus status;

    public OcrValidationResult(
        List<String> warnings,
        List<String> errors
    ) {
        if (isNotEmpty(errors)) {
            this.status = ValidationStatus.ERRORS;
        } else {
            this.status = ValidationStatus.SUCCESS;
        }

        this.warnings = warnings;
        this.errors = errors;
    }

}