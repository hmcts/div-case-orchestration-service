package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class CcdCalllbackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    @Autowired
    private ValidateCaseData validateCaseData;

    @Autowired
    private PetitionGenerator petitionGenerator;

    @Autowired
    private RespondentLetterGenerator respondentLetterGenerator;

    @Autowired
    private IdamPinGenerator idamPinGenerator;

    @Autowired
    private CaseDataFormatter caseDataFormatter;

    public Map<String, Object> run(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {
        Map<String, Object> payLoad = caseDetailsRequest.getCaseDetails().getCaseData();

        petitionGenerator.setup(authToken, caseDetailsRequest.getCaseDetails());
        idamPinGenerator.setup(authToken);
        respondentLetterGenerator.setup(authToken, caseDetailsRequest.getCaseDetails());

        return this.execute(new Task[] {
            validateCaseData,
                petitionGenerator,
                idamPinGenerator,
                respondentLetterGenerator,
                caseDataFormatter
        }, payLoad);
    }
}
