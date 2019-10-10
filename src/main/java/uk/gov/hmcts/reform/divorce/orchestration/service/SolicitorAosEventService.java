package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.domain.SubmitSolicitorAosEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface SolicitorAosEventService {

    Map<String, Object> fireSecondaryAosEvent(SubmitSolicitorAosEvent event) throws WorkflowException;

}
