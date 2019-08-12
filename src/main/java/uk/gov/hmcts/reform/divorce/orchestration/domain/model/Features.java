package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_SOLICITOR_DETAILS("feature_resp_solicitor_details"),
    DN_REFUSAL("dn_refusal");

    private final String name;
}
