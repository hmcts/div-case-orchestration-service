package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdEvents {

    public static final String AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED = "startAosFromServiceAppRejected";
    public static final String AOS_NOT_RECEIVED_FOR_PROCESS_SERVER_EVENT_ID = "aosNotReceivedForProcessServer";

}
