package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_SOLICITOR_DETAILS("feature_resp_solicitor_details"),
    DN_REFUSAL("dn_refusal"),
    PAPER_UPDATE("paper_update"),
    SOLICITOR_DN_REJECT_AND_AMEND("solicitor_dn_reject_and_amend");

    private final String name;
}
