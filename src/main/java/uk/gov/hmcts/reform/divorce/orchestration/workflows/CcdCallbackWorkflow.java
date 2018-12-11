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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_SERVICE_CENTRE;

@Component
public class CcdCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final SetIssueDate setIssueDate;
    private final ValidateCaseData validateCaseData;
    private final PetitionGenerator petitionGenerator;
    private final IdamPinGenerator idamPinGenerator;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CaseFormatterAddPDF caseFormatterAddPDF;

    @Autowired
    public CcdCallbackWorkflow(ValidateCaseData validateCaseData,
                               SetIssueDate setIssueDate,
                               PetitionGenerator petitionGenerator,
                               IdamPinGenerator idamPinGenerator,
                               RespondentLetterGenerator respondentLetterGenerator,
                               CaseFormatterAddPDF caseFormatterAddPDF) {
        this.validateCaseData = validateCaseData;
        this.setIssueDate = setIssueDate;
        this.petitionGenerator = petitionGenerator;
        this.respondentLetterGenerator = respondentLetterGenerator;
        this.idamPinGenerator = idamPinGenerator;
        this.caseFormatterAddPDF = caseFormatterAddPDF;
    }

    public Map<String, Object> run(CreateEvent caseDetailsRequest,
                                   String authToken, boolean generateAosInvitation) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();

        tasks.add(validateCaseData);
        tasks.add(setIssueDate);
        tasks.add(petitionGenerator);
        tasks.add(idamPinGenerator);

        if (generateAosInvitation && isServiceCentreDivorceUnit(caseDetailsRequest.getCaseDetails().getCaseData())) {
            tasks.add(respondentLetterGenerator);
        }

        tasks.add(caseFormatterAddPDF);

        return this.execute(
            tasks.toArray(new Task[tasks.size()]),
            caseDetailsRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetailsRequest.getCaseDetails())
        );
    }

    private boolean isServiceCentreDivorceUnit(Map<String, Object> caseData) {
        return DIVORCE_UNIT_SERVICE_CENTRE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_UNIT_JSON_KEY)));
    }
}
