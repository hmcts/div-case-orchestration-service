package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeNisiService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetDNGrantedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SingleCaseDocumentGenerationWorkflow;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecreeNisiServiceImpl implements DecreeNisiService {

    private final SetDNGrantedDateWorkflow setDNGrantedDateWorkflow;
    private final SingleCaseDocumentGenerationWorkflow singleCaseDocumentGenerationWorkflow;

    @Override
    public Map<String, Object> setDNGrantedManual(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        return setDNGrantedDateWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> handleManualDnPronouncementDocumentGeneration(final CcdCallbackRequest ccdCallbackRequest, final String authToken)
        throws WorkflowException {
        return singleCaseDocumentGenerationWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
    }
}
