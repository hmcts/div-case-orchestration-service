package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDnOutcomeFlagFieldTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DecreeNisiRefusalDocumentGeneratorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DefineWhoPaysCostsOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNDecisionStateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateDNDecisionTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class DecreeNisiAboutToBeGrantedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ValidateDNDecisionTask validateDNDecisionTask;

    private final AddDecreeNisiDecisionDateTask addDecreeNisiDecisionDateTask;

    private final DefineWhoPaysCostsOrderTask defineWhoPaysCostsOrderTask;

    private final AddDnOutcomeFlagFieldTask addDnOutcomeFlagFieldTask;

    private final SetDNDecisionStateTask setDNDecisionStateTask;

    private final DecreeNisiRefusalDocumentGeneratorTask decreeNisiRefusalDocumentGeneratorTask;

    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        List<Task> tasksToRun = new ArrayList<>();

        Map<String, Object> caseData = caseDetails.getCaseData();
        tasksToRun.add(setDNDecisionStateTask);
        tasksToRun.add(validateDNDecisionTask);
        tasksToRun.add(addDecreeNisiDecisionDateTask);
        Object decreeNisiGranted = caseData.get(DECREE_NISI_GRANTED_CCD_FIELD);

        if (YES_VALUE.equals(decreeNisiGranted)) {
            tasksToRun.add(addDnOutcomeFlagFieldTask);
            Object costsClaimGranted = caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD);
            if (YES_VALUE.equals(costsClaimGranted)) {
                tasksToRun.add(defineWhoPaysCostsOrderTask);
            }
        }

        tasksToRun.add(decreeNisiRefusalDocumentGeneratorTask);
        tasksToRun.add(caseFormatterAddDocuments);

        Map<String, Object> payloadToReturn = this.execute(
            tasksToRun.stream().toArray(Task[]::new),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );

        return payloadToReturn;
    }

}