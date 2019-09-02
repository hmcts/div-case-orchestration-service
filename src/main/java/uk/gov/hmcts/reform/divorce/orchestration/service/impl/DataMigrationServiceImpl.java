package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataMigrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow;

import java.time.LocalDate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService {

    @Autowired
    private FamilyManDataMigrationWorkflow workflow;

    @Override
    public void migrateCasesToFamilyMan(DataMigrationRequest.Status status, LocalDate date, String authToken)
        throws CaseOrchestrationServiceException {

        try {
            workflow.run(status, date, authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

}