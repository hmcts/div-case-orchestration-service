package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

public interface BulkCaseService {

    void handleBulkCaseCreateEvent(BulkCaseCreateEvent event);

    void handleBulkCaseUpdateCourtHearingEvent(BulkCaseUpdateCourtHearingEvent event) throws WorkflowException;
}
