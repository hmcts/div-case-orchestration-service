package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.CleanStatusEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseEventService {

    Map<String, Object> cleanStateFromData(CleanStatusEvent event) throws WorkflowException;

}
