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
        log.info("Listened to {} for caseId [{}]", aosOfflineTriggerRequestEvent.getClass().getSimpleName(), caseId);

        try {
            aosService.triggerAosOfflineForCase(authUtil.getCaseworkerToken(), caseId);
            log.info("AOS offline issued for Case {}", caseId);
        } catch (CaseOrchestrationServiceException exception) {
            RuntimeException runtimeException = new RuntimeException("Error issuing AOS offline for caseId [" + caseId + "]", exception);
            log.error(runtimeException.getMessage(), runtimeException);
            throw runtimeException;
        }
    }
}