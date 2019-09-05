package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow;

import java.time.LocalDate;

@Service
public class DataExtractionServiceImpl implements DataExtractionService {

    @Autowired
    private FamilyManDataExtractionWorkflow workflow;

    @Override
    public void extractCasesToFamilyMan(DataExtractionRequest.Status status, LocalDate date, String authToken)
        throws CaseOrchestrationServiceException {

        try {
            workflow.run(status, date, authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

}