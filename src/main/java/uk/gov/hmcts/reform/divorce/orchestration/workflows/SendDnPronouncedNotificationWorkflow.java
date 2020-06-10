package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCostOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@AllArgsConstructor
@Slf4j
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;
    private final SendRespondentGenericUpdateNotificationEmail sendRespondentGenericUpdateNotificationEmail;
    private final SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;
    private final SendCostOrderGenerationTask sendCostOrderGenerationTask;
    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        Map<String, Object> caseDataToReturn = this.execute(
            getTasks(caseData),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );

        return caseDataToReturn;
    }

    private Task[] getTasks(Map<String, Object> caseData) {
        List<Task> tasks = new ArrayList<>();

        if(isCoRespContactMethodIsDigital(caseData)) {
            tasks.add(sendPetitionerGenericUpdateNotificationEmail); // TODO: rename, add task suffix
            tasks.add(sendRespondentGenericUpdateNotificationEmail); // TODO: rename, add task suffix

            if (isCoRespondentLiableForCosts(caseData)) {
                tasks.add(sendCoRespondentGenericUpdateNotificationEmail); // TODO: rename, add task suffix
            }
        } else {
           if(isPaperUpdateEnabled()) {
               tasks.add(sendCostOrderGenerationTask);
           }
        }

        return tasks.toArray(new Task[0]);
    }

    private boolean isCoRespContactMethodIsDigital(Map<String, Object> caseData) { //TODO maybe move to PartyRepresentedChecker so can be tested?
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL)));
    }

    private boolean isCoRespondentLiableForCosts(Map<String, Object> caseData) { //TODO maybe move to PartyRepresentedChecker so can be tested?
        String whoPaysCosts = String.valueOf(caseData.get(WHO_PAYS_COSTS_CCD_FIELD));

        return WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT.equalsIgnoreCase(whoPaysCosts)
            || WHO_PAYS_CCD_CODE_FOR_BOTH.equalsIgnoreCase(whoPaysCosts);
    }

    private boolean isPaperUpdateEnabled(){
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }
}
