package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetClaimCostsFrom;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorCourtDetailsTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SolicitorCreateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask;
    private final AddMiniPetitionDraftTask addMiniPetitionDraftTask;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final SetClaimCostsFrom setClaimCostsFrom;

    @Autowired
    public SolicitorCreateWorkflow(
        SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask,
        AddMiniPetitionDraftTask addMiniPetitionDraftTask,
        CaseFormatterAddDocuments caseFormatterAddDocuments,
        SetClaimCostsFrom setClaimCostsFrom) {
        this.setSolicitorCourtDetailsTask = setSolicitorCourtDetailsTask;
        this.addMiniPetitionDraftTask = addMiniPetitionDraftTask;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
        this.setClaimCostsFrom = setClaimCostsFrom;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        final List<Task> tasks = new ArrayList<>();
        if (isPetitionerClaimingCostsAndClaimCostsFromIsEmptyIn(caseDetails)) {
            tasks.add(setClaimCostsFrom);
        }
        tasks.add(setSolicitorCourtDetailsTask);
        tasks.add(addMiniPetitionDraftTask);
        tasks.add(caseFormatterAddDocuments);
        return this.execute(tasks.toArray(new Task[0]),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }

    private boolean isPetitionerClaimingCostsAndClaimCostsFromIsEmptyIn(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        boolean isPetitionerClaimingCosts = YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_CCD_FIELD)));
        boolean claimCostsFromIsEmpty = StringUtils.isEmpty(caseData.get(DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD))
            || CollectionUtils.isEmpty(Arrays.asList(caseData.get(DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD)));
        return isPetitionerClaimingCosts && claimCostsFromIsEmpty;
    }
}
