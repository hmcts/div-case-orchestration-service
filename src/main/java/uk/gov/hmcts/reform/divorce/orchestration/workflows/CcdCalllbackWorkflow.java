package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.IdamPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class CcdCalllbackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final ValidateCaseData validateCaseData;

    private final PetitionGenerator petitionGenerator;

    private final IdamPinGenerator idamPinGenerator;

    private final RespondentLetterGenerator respondentLetterGenerator;

    private final CaseDataFormatter caseDataFormatter;

    @Autowired
    public CcdCalllbackWorkflow(ValidateCaseData validateCaseData,
                                PetitionGenerator petitionGenerator,
                                IdamPinGenerator idamPinGenerator,
                                RespondentLetterGenerator respondentLetterGenerator,
                                CaseDataFormatter caseDataFormatter) {
        this.validateCaseData = validateCaseData;
        this.petitionGenerator = petitionGenerator;
        this.respondentLetterGenerator = respondentLetterGenerator;
        this.idamPinGenerator = idamPinGenerator;
        this.caseDataFormatter = caseDataFormatter;
    }

    public Map<String, Object> run(CreateEvent caseDetailsRequest,
                                   String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                validateCaseData,
                petitionGenerator,
                idamPinGenerator,
                respondentLetterGenerator,
                caseDataFormatter
            },
            caseDetailsRequest.getCaseDetails().getCaseData(),
            new ImmutablePair(AUTH_TOKEN_JSON_KEY, authToken),
            new ImmutablePair(CASE_DETAILS_JSON_KEY, caseDetailsRequest.getCaseDetails())
        );
    }
}
