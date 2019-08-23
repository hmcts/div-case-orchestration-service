package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

public interface ValidationService {

    ValidationResponse validate(ValidationRequest validationRequest);
}
