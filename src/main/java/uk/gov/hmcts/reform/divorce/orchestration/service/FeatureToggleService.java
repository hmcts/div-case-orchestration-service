package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;

public interface FeatureToggleService {
    boolean isFeatureEnabled(Features feature);
}
