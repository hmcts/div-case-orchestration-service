package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddPetitionerSolicitorRoleTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class PetitionerSolicitorRoleWorkflow extends DefaultWorkflow<Map<String, Object>> {


    private AddPetitionerSolicitorRoleTask addPetitionerSolicitorRoleTask;

    @Autowired
    public PetitionerSolicitorRoleWorkflow(AddPetitionerSolicitorRoleTask addPetitionerSolicitorRoleTask) {
        this.addPetitionerSolicitorRoleTask = addPetitionerSolicitorRoleTask;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken) throws WorkflowException {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        return this.execute(
                new Task[] {
                    addPetitionerSolicitorRoleTask
                },
                ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

}
