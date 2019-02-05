package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

@Component
public class AmendPetitionWorkflow extends DefaultWorkflow<Map<String, Object>> {


    private final CreateAmendPetitionDraft amendPetitionDraft;
    private final UpdateCaseInCCD updateCaseInCCD;

    @Autowired
    public AmendPetitionWorkflow(CreateAmendPetitionDraft amendPetitionDraft,
                                 UpdateCaseInCCD updateCaseInCCD) {
        this.amendPetitionDraft = amendPetitionDraft;
        this.updateCaseInCCD = updateCaseInCCD;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> run(String caseId, String authToken) throws WorkflowException {
        this.execute(
                new Task[]{
                    amendPetitionDraft,
                    updateCaseInCCD
                },
            new HashMap<>(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, DN_REJECTED)
        );

        return (Map<String, Object>) getContext()
            .getTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY);
    }
}