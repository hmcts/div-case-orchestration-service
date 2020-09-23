package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;
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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ListElements.CO_RESPONDENT_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ListElements.OTHER_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ListElements.PETITIONER_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ListElements.RESPONDENT_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.getGeneralEmailParties;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isOtherPartyDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

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

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} GeneralEmailWorkflow workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        String party = getGeneralEmailParties(caseData);

        if (party.equals(PETITIONER_GENERAL_EMAIL_SELECTION)) {
            if (isPetitionerRepresented(caseData)) {
                tasks.add(getGeneralEmailPetitionerSolicitorTask(caseId));
            } else {
                tasks.add(getGeneralEmailPetitionerTask(caseId));
            }
        } else if (party.equals(RESPONDENT_GENERAL_EMAIL_SELECTION)) {
            if (isRespondentRepresented(caseData)) {
                tasks.add(getGeneralEmailRespondentSolicitorTask(caseId));
            } else if (isRespondentDigital(caseData)) {
                tasks.add(getGeneralEmailRespondentTask(caseId));
            }
        } else if (party.equals(CO_RESPONDENT_GENERAL_EMAIL_SELECTION)) {
            if (isCoRespondentRepresented(caseData)) {
                tasks.add(getGeneralEmailCoRespondentSolicitorTask(caseId));
            } else if (isCoRespondentDigital(caseData)) {
                tasks.add(getGeneralEmailCoRespondentTask(caseId));
            }
        } else if (party.equals(OTHER_GENERAL_EMAIL_SELECTION)) {
            if (isOtherPartyDigital(caseData)) {
                tasks.add(getGeneralEmailOtherPartyTask(caseId));
            }
        } else {
            log.error("CaseID: {} GeneralEmailWorkflow workflow has received invalid general email parties data.", caseId);
            throw new TaskException("CaseData could not be build with invalid Party declaration.");
        }

        return tasks.toArray(new Task[]{});
    }

    private Task<Map<String, Object>> getGeneralEmailCoRespondentTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.CO_RESPONDENT);
        return generalEmailCoRespondentTask;
    }

    private Task<Map<String, Object>> getGeneralEmailCoRespondentSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.CO_RESPONDENT_SOLICITOR);
        return generalEmailCoRespondentSolicitorTask;
    }

    private Task<Map<String, Object>> getGeneralEmailOtherPartyTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.OTHER);
        return generalEmailOtherPartyTask;
    }

    private Task<Map<String, Object>> getGeneralEmailPetitionerSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR);
        return generalEmailPetitionerSolicitorTask;
    }

    private Task<Map<String, Object>> getGeneralEmailPetitionerTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.PETITIONER);
        return generalEmailPetitionerTask;
    }

    private Task<Map<String, Object>> getGeneralEmailRespondentTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.RESPONDENT);
        return generalEmailRespondentTask;
    }

    private Task<Map<String, Object>> getGeneralEmailRespondentSolicitorTask(String caseId) {
        log.info("CaseId: {} Executing task to send general email to {}", caseId, GeneralEmailTaskHelper.Party.RESPONDENT_SOLICITOR);
        return generalEmailRespondentSolicitorTask;
    }
}