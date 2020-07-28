package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosOverdueRequestListener implements ApplicationListener<AosOverdueRequest> {

    private final AuthUtil authUtil;
    private final AosService aosService;

    @Override
    public void onApplicationEvent(AosOverdueRequest aosOverdueRequest) {
        String caseId = aosOverdueRequest.getCaseId();
        log.info("Listened to request to make case [{}] overdue.", caseId);
        try {
            aosService.makeCaseAosOverdue(authUtil.getCaseworkerToken(), caseId);
        } catch (CaseOrchestrationServiceException e) {
            RuntimeException runtimeException = new RuntimeException(format("Error trying to move case %s to AOS Overdue state", caseId), e);
            log.error(runtimeException.getMessage(), runtimeException);
            throw runtimeException;
        }
    }

}