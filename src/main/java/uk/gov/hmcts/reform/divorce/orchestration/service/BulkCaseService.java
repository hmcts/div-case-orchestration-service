package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;

public interface BulkCaseService {

    void handleBulkCaseCreateEvent(BulkCaseCreateEvent event);
}
