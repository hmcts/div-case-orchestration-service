package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AOSOfflineTriggerRequestEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@AllArgsConstructor
@Slf4j
@Component
public class AOSOfflineTriggerRequestListener implements ApplicationListener<AOSOfflineTriggerRequestEvent> {

    private final AuthUtil authUtil;
    private final AosService aosService;

    @Override
    public void onApplicationEvent(AOSOfflineTriggerRequestEvent aosOfflineTriggerRequestEvent) {
        String caseId = aosOfflineTriggerRequestEvent.getCaseId();
        log.info("CaseID: {} Listened to {} event", caseId, aosOfflineTriggerRequestEvent.getClass().getSimpleName());

        try {
            aosService.triggerAosOfflineForCase(authUtil.getCaseworkerToken(), caseId);
            log.info("CaseID: {} AOS offline issued", caseId);
        } catch (CaseOrchestrationServiceException exception) {
            RuntimeException runtimeException = new RuntimeException("CaseID: " + caseId + " Error issuing AOS offline.", exception);
            log.error(runtimeException.getMessage(), runtimeException);
            throw runtimeException;
        }
    }
}