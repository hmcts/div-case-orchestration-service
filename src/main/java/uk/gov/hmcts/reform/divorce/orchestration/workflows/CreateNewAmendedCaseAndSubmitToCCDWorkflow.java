package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyPetitionerSolicitorDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraftForRefusalFromCaseIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetAmendedCaseIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SolicitorSubmitCaseToCCDTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FOR_REFUSAL_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;

@Component
public class CreateNewAmendedCaseAndSubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CreateAmendPetitionDraftForRefusalFromCaseIdTask createAmendPetitionDraftForRefusalFromCaseId;
    private final FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;
    private final CopyPetitionerSolicitorDetailsTask copyPetitionerSolicitorDetailsTask;
    private final ValidateCaseDataTask validateCaseDataTask;
    private final SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCD;
    private final SetAmendedCaseIdTask setAmendedCaseIdTask;

    @Autowired
    public CreateNewAmendedCaseAndSubmitToCCDWorkflow(
        CreateAmendPetitionDraftForRefusalFromCaseIdTask amendPetitionDraftForRefusalFromCaseId,
        FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask, CopyPetitionerSolicitorDetailsTask copyPetitionerSolicitorDetailsTask,
        ValidateCaseDataTask validateCaseDataTask, SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCD,
        SetAmendedCaseIdTask setAmendedCaseIdTask) {
        this.createAmendPetitionDraftForRefusalFromCaseId = amendPetitionDraftForRefusalFromCaseId;
        this.formatDivorceSessionToCaseDataTask = formatDivorceSessionToCaseDataTask;
        this.copyPetitionerSolicitorDetailsTask = copyPetitionerSolicitorDetailsTask;
        this.validateCaseDataTask = validateCaseDataTask;
        this.solicitorSubmitCaseToCCD = solicitorSubmitCaseToCCD;
        this.setAmendedCaseIdTask = setAmendedCaseIdTask;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        this.execute(
            new Task[]{
                createAmendPetitionDraftForRefusalFromCaseId,
                formatDivorceSessionToCaseDataTask,
                copyPetitionerSolicitorDetailsTask,
                validateCaseDataTask,
                solicitorSubmitCaseToCCD,
                setAmendedCaseIdTask
            },
            new HashMap<>(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(CCD_CASE_DATA, caseDetails.getCaseData()),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, AMEND_PETITION_FOR_REFUSAL_EVENT)
        );

        return getContext().getTransientObject(CCD_CASE_DATA);
    }
}
