package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddPDF;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.IdamPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class CcdCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final ValidateCaseData validateCaseData;
    private final PetitionGenerator petitionGenerator;
    private final IdamPinGenerator idamPinGenerator;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CaseFormatterAddPDF caseFormatterAddPDF;

    @Autowired
    public CcdCallbackWorkflow(ValidateCaseData validateCaseData,
                               PetitionGenerator petitionGenerator,
                               IdamPinGenerator idamPinGenerator,
                               RespondentLetterGenerator respondentLetterGenerator,
                               CaseFormatterAddPDF caseFormatterAddPDF) {
        this.validateCaseData = validateCaseData;
        this.petitionGenerator = petitionGenerator;
        this.respondentLetterGenerator = respondentLetterGenerator;
        this.idamPinGenerator = idamPinGenerator;
        this.caseFormatterAddPDF = caseFormatterAddPDF;
    }

    public Map<String, Object> run(CreateEvent caseDetailsRequest,
                                   String authToken, boolean generateAosInvitation) throws WorkflowException {

        List<Task> tasks = Arrays.asList(validateCaseData, petitionGenerator, idamPinGenerator);

        if(generateAosInvitation) {
            tasks.add(respondentLetterGenerator);
        }

        tasks.add(caseFormatterAddPDF);

        return this.execute(
            (Task[])tasks.toArray(),
            caseDetailsRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetailsRequest.getCaseDetails())
        );
    }
}
