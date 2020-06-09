package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Slf4j
@Component
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private FeatureToggleService featureToggleService;

    @Autowired
    SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;

    @Autowired
    SendRespondentGenericUpdateNotificationEmail sendRespondentGenericUpdateNotificationEmail;

    @Autowired
    SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();


        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        // Scenario 1: Paper corespondent with cost claim was granted and who is not represented -  FL-DIV-LET-ENG-00358.docx
        // Scenario 2: Paper corespondent with cost claim was granted and who is represented - FL-DIV-GNO-ENG-00423.docx

        // Scenario 3: Paper corespondent and the cost claim was not granted

        // Scenario 4: Digital corespondent~


         if (isPaperUpdateEnabled()) {
             log.trace("Feature toggle paper_update is enabled. CaseID: {} ", caseId);

             if (isCoRespContactMethodPaperBased(caseData)){

                 if (isCostsClaimGranted(caseData)) {

                     if (isCoRespondentRepresented(caseData)) {
                         log.trace("Paper correspondent with cost claim was granted is represented. CaseID: {} ", caseId);
                         // Scenario 2: Paper corespondent with cost claim was granted and who is represented
                     } else {
                         log.trace("Paper correspondent with cost claim was granted is not represented. CaseID: {} ", caseId);
                         // Scenario 1: Paper corespondent with cost claim was granted and who is not represented
                         }
                 } else {
                     log.trace("Paper Correspondent and cost claim not granted. CaseID: {} ", caseId);
                     // Scenario 3: Paper correspondent and the cost claim was not granted
                 }
             } else {
                 sendGenericUpdateNotificationEmails(tasks, ccdCallbackRequest, caseId, isPaperUpdateEnabled());
             }
         } else {
             sendGenericUpdateNotificationEmails(tasks, ccdCallbackRequest, caseId, isPaperUpdateEnabled());
         }

        return this.execute(
            tasks.toArray(new Task[0]),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private void sendGenericUpdateNotificationEmails(List<Task> tasks, CcdCallbackRequest ccdCallbackRequest, String caseId, boolean isPaperUpdateEnabled){
        log.trace("Feature toggle paper_update status is {}. CaseID: {} ", isPaperUpdateEnabled, caseId);
        // Scenario 4: Digital correspondent

        tasks.add(sendPetitionerGenericUpdateNotificationEmail);
        tasks.add(sendRespondentGenericUpdateNotificationEmail);

        if (isCoRespondentLiableForCosts(ccdCallbackRequest.getCaseDetails().getCaseData())) {
            tasks.add(sendCoRespondentGenericUpdateNotificationEmail);
        }
    }

    private boolean isCoRespondentLiableForCosts(Map<String, Object> caseData) {
        String whoPaysCosts = String.valueOf(caseData.get(WHO_PAYS_COSTS_CCD_FIELD));

        return WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT.equalsIgnoreCase(whoPaysCosts)
            || WHO_PAYS_CCD_CODE_FOR_BOTH.equalsIgnoreCase(whoPaysCosts);
    }

    private boolean isCostsClaimGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD)));
    }

    private boolean isCoRespContactMethodPaperBased(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL)));
    }

    private boolean isPaperUpdateEnabled(){
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

}
