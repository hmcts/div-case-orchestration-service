package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;

import java.util.Map;

public interface ExceptionRecordToCaseTransformer {
    Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord);
}
