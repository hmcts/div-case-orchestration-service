package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DataExtractionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow;

import java.time.LocalDate;

@Slf4j
@Service
public class DataExtractionServiceImpl implements DataExtractionService {

    @Autowired
    private FamilyManDataExtractionWorkflow workflow;
  
    @Autowired
    private DataExtractionWorkflow dataExtractionWorkflow;
  
    @Override
    public void requestDataExtraction() throws WorkflowException {
        log.info("Data Extraction: Sending case status and yesterday's date");
        dataExtractionWorkflow.run();
        log.info("Data Extraction: Completed sending case status and yesterday's date");
    }

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
