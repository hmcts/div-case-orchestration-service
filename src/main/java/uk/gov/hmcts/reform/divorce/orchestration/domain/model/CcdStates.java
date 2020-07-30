package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdStates {
    public static final String SERVICE_APPLICATION_NOT_APPROVED = "ServiceApplicationNotApproved";
    public static final String AWAITING_PAYMENT = "AwaitingPayment";
    public static final String AWAITING_HWF_DECISION = "AwaitingHWFDecision";
    public static final String AWAITING_DN_APPLICATION = "AwaitingDNApplication";
}
