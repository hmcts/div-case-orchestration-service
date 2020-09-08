package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailCoRespondentSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailCoRespondentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailOtherPartyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailPetitionerSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailPetitionerTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailRespondentSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailRespondentTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralEmailWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralEmailCoRespondentSolicitorTask generalEmailCoRespondentSolicitorTask;
    private final GeneralEmailCoRespondentTask generalEmailCoRespondentTask;
    private final GeneralEmailOtherPartyTask generalEmailOtherPartyTask;
    private final GeneralEmailPetitionerSolicitorTask generalEmailPetitionerSolicitorTask;
    private final GeneralEmailPetitionerTask generalEmailPetitionerTask;
    private final GeneralEmailRespondentSolicitorTask generalEmailRespondentSolicitorTask;
    private final GeneralEmailRespondentTask generalEmailRespondentTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        log.info("CaseID: {} ServiceDecisionMade workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(ccdCallbackRequest.getCaseDetails()),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(getGeneralEmailPetitionerTask(caseId));
        tasks.add(getGeneralEmailPetitionerSolicitorTask(caseId));

        tasks.add(getGeneralEmailRespondentTask(caseId));
        tasks.add(getGeneralEmailRespondentSolicitorTask(caseId));

        tasks.add(getGeneralEmailCoRespondentSolicitorTask(caseId));
        tasks.add(getGeneralEmailCoRespondentTask(caseId));

        tasks.add(getGeneralEmailOtherPartyTask(caseId));

        return tasks.toArray(new Task[] {});
    }

    private Task<Map<String, Object>> getGeneralEmailCoRespondentTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to CoRespondent", caseId);
        return generalEmailCoRespondentTask;
    }

    private Task<Map<String, Object>> getGeneralEmailCoRespondentSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to CoRespondent Solicitor", caseId);
        return generalEmailCoRespondentSolicitorTask;
    }

    private Task<Map<String, Object>> getGeneralEmailOtherPartyTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to Other party", caseId);
        return generalEmailOtherPartyTask;
    }

    private Task<Map<String, Object>> getGeneralEmailPetitionerSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to Petitioner Solicitor", caseId);
        return generalEmailPetitionerSolicitorTask;
    }

    private Task<Map<String, Object>> getGeneralEmailPetitionerTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to Petitioner", caseId);
        return generalEmailPetitionerTask;
    }

    private Task<Map<String, Object>> getGeneralEmailRespondentTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to Petitioner", caseId);
        return generalEmailRespondentTask;
    }

    private Task<Map<String, Object>> getGeneralEmailRespondentSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to Petitioner", caseId);
        return generalEmailRespondentSolicitorTask;
    }
}