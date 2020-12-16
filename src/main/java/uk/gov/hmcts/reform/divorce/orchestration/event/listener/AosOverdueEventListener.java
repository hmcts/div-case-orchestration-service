package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForAlternativeMethodCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForProcessServerCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosOverdueEventListener implements ApplicationListener<AosOverdueEvent> {

    private final AuthUtil authUtil;
    private final AosService aosService;

    @Override
    public void onApplicationEvent(AosOverdueEvent aosOverdueEvent) {
        String caseId = aosOverdueEvent.getCaseId();
        String eventClassName = aosOverdueEvent.getClass().getSimpleName();
        log.info("Listened to {} event to make case [{}] overdue.", eventClassName, caseId);

        try {
            if (aosOverdueEvent instanceof AosOverdueForAlternativeMethodCaseEvent) {
                processAosOverdueForAlternativeMethodCaseEvent(aosOverdueEvent);
            } else if (aosOverdueEvent instanceof AosOverdueForProcessServerCaseEvent) {
                processAosOverdueForProcessServerCaseEvent(aosOverdueEvent);
            } else {
                processAosOverdueCaseEvent(aosOverdueEvent);
            }
        } catch (CaseOrchestrationServiceException e) {
            RuntimeException runtimeException = new RuntimeException(format("Error processing %s event for case %s", eventClassName, caseId), e);
            log.error(runtimeException.getMessage(), runtimeException);
            throw runtimeException;
        }
    }

    private void processAosOverdueForAlternativeMethodCaseEvent(AosOverdueEvent event) throws CaseOrchestrationServiceException {
        aosService.markAosNotReceivedForAlternativeMethodCase(authUtil.getCaseworkerToken(), event.getCaseId());
    }

    private void processAosOverdueForProcessServerCaseEvent(AosOverdueEvent event) throws CaseOrchestrationServiceException {
        aosService.markAosNotReceivedForProcessServerCase(authUtil.getCaseworkerToken(), event.getCaseId());
    }

    private void processAosOverdueCaseEvent(AosOverdueEvent event) throws CaseOrchestrationServiceException {
        aosService.makeCaseAosOverdue(authUtil.getCaseworkerToken(), event.getCaseId());
    }

}