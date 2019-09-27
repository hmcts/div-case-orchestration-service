package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseAcceptedCasesEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCancelPronouncementEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseRemovePronouncementDetailsEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdatePronouncementDateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface BulkCaseService {

    void handleBulkCaseCreateEvent(BulkCaseCreateEvent event) throws WorkflowException;

    void handleBulkCaseUpdateCourtHearingEvent(BulkCaseUpdateCourtHearingEvent event) throws WorkflowException;

    void handleBulkCaseUpdatePronouncementDateEvent(BulkCaseUpdatePronouncementDateEvent event) throws WorkflowException;

    void handleBulkCaseAcceptedCasesEvent(BulkCaseAcceptedCasesEvent event);

    void handleBulkCaseRemovePronouncementDetailsEvent(BulkCaseRemovePronouncementDetailsEvent event);

    Map<String, Object> removeFromBulkListed(CcdCallbackRequest callbackRequest, String auth) throws WorkflowException;

    void handleBulkCaseCancelPronouncementDetailsEvent(BulkCaseCancelPronouncementEvent event) throws WorkflowException;

}
