package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_SOLICITOR_DETAILS("feature_resp_solicitor_details"),
    DN_REFUSAL("dn_refusal"),
    PAPER_UPDATE("paper_update"),
    SOLICITOR_DN_REJECT_AND_AMEND("solicitor_dn_reject_and_amend"),
    PAY_BY_ACCOUNT("pay_by_account"),
    SHARE_A_CASE("share_a_case");

    private final String name;

}