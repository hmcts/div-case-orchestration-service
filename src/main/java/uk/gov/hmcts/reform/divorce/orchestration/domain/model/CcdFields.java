package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdFields {
    public static final String SERVICE_APPLICATION_TYPE = "ServiceApplicationType";
    public static final String SERVICE_APPLICATION_GRANTED = "ServiceApplicationGranted";
    public static final String SERVICE_APPLICATION_DECISION_DATE = "ServiceApplicationDecisionDate";
    public static final String RECEIVED_SERVICE_APPLICATION_DATE = "ReceivedServiceApplicationDate";
}
