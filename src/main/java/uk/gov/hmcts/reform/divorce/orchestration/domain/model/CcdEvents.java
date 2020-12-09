package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdEvents {

    public static final String AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED = "startAosFromServiceAppRejected";
    public static final String CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE_EVENT_ID = "co-RespAOSReceivedFromAwaitingAlternativeService";
    public static final String CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE_EVENT_ID = "co-RespAOSReceivedFromAwaitingProcessServerService";
    public static final String CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE_EVENT_ID = "co-RespAOSReceivedFromAwaitingDWPResponse";
}
