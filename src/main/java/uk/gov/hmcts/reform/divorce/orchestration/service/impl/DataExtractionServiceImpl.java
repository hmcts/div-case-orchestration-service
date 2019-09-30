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
    public void requestDataExtractionForPreviousDay() throws CaseOrchestrationServiceException {
        try {
            log.info("Data Extraction: Requesting data extraction");
            dataExtractionWorkflow.run();
            log.info("Data Extraction: Completed requesting data extraction");
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public void extractCasesToFamilyMan(DataExtractionRequest.Status status, LocalDate date, String authToken)
        throws CaseOrchestrationServiceException {

        log.info("Extracting data for status {} and date {}", status, date);

        try {
            workflow.run(status, date, authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

}