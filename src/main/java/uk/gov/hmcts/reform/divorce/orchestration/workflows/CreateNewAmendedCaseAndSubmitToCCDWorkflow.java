package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraftForRefusalFromCaseIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SolicitorSubmitCaseToCCDTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FOR_REFUSAL_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class CreateNewAmendedCaseAndSubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CreateAmendPetitionDraftForRefusalFromCaseIdTask createAmendPetitionDraftForRefusalFromCaseId;
    private final UpdateCaseInCCD updateCaseInCCD;
    private final FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;
    private final ValidateCaseDataTask validateCaseDataTask;
    private final SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCD;

    @Autowired
    public CreateNewAmendedCaseAndSubmitToCCDWorkflow(
        CreateAmendPetitionDraftForRefusalFromCaseIdTask amendPetitionDraftForRefusalFromCaseId,
        UpdateCaseInCCD updateCaseInCCD, FormatDivorceSessionToCaseData formatDivorceSessionToCaseData,
        ValidateCaseDataTask validateCaseDataTask, SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCD) {
        this.createAmendPetitionDraftForRefusalFromCaseId = amendPetitionDraftForRefusalFromCaseId;
        this.updateCaseInCCD = updateCaseInCCD;
        this.formatDivorceSessionToCaseData = formatDivorceSessionToCaseData;
        this.validateCaseDataTask = validateCaseDataTask;
        this.solicitorSubmitCaseToCCD = solicitorSubmitCaseToCCD;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        this.execute(
            new Task[]{
                createAmendPetitionDraftForRefusalFromCaseId,
                formatDivorceSessionToCaseData,
                validateCaseDataTask,
                solicitorSubmitCaseToCCD,
                updateCaseInCCD

            },
            new HashMap<>(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, AMEND_PETITION_FOR_REFUSAL_EVENT)
        );

        return caseDetails.getCaseData();
    }
}
