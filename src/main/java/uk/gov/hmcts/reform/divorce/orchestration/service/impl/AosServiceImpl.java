package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerAOSOverdueNotificationWorkflow;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AosServiceImpl implements AosService {
    private final SendPetitionerAOSOverdueNotificationWorkflow sendPetitionerAOSOverdueNotificationEmail;

    @Override
    public Map<String, Object> sendPetitionerAOSOverdueNotificationEmail(CcdCallbackRequest ccdCallbackRequest)
            throws WorkflowException {
        return sendPetitionerAOSOverdueNotificationEmail.run(ccdCallbackRequest);
    }
}
