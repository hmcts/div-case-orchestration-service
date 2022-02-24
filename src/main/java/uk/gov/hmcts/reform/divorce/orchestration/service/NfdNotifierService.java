package uk.gov.hmcts.reform.divorce.orchestration.service;

public interface NfdNotifierService {

    void notifyUnsubmittedApplications() throws CaseOrchestrationServiceException;
}
