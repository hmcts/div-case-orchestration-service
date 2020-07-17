package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@Component
@RequiredArgsConstructor
public class AosOverdueRequestListener implements ApplicationListener<AosOverdueRequest> {

    private final AuthUtil authUtil;
    private final CaseOrchestrationService caseOrchestrationService;

    @Override
    public void onApplicationEvent(AosOverdueRequest aosOverdueRequest) {
        caseOrchestrationService.makeCaseAosOverdue(authUtil.getCaseworkerToken(), aosOverdueRequest.getCaseId());
    }

}