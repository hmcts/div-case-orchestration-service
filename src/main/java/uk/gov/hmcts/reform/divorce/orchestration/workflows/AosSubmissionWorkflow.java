package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitRespondentAosCaseForSolicitorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
public class AosSubmissionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String NOT_DEFENDING_NOT_ADMITTING = "NoNoAdmission";

    @Autowired
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail
        sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;

    @Autowired
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail
        sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    @Autowired
    private SubmitRespondentAosCaseForSolicitorTask
        submitRespondentAosCaseForSolicitorTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        String defended = (String) caseData.get(RESP_WILL_DEFEND_DIVORCE);

        List<Task> tasks = new ArrayList<>();

        if (isSolicitorRepresentingRespondent(caseData) || isRespondentSolicitorInformationPresent(caseData)) {
            // submitRespondentAosCaseForSolicitorTask will add values for RESP_WILL_DEFEND_DIVORCE
            tasks.add(submitRespondentAosCaseForSolicitorTask);
        } else {
            if  (YES_VALUE.equalsIgnoreCase(defended)) {
                tasks.add(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask);
            } else if (NO_VALUE.equalsIgnoreCase(defended) || NOT_DEFENDING_NOT_ADMITTING.equalsIgnoreCase(defended)) {
                tasks.add(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask);
            } else {
                String errorMessage = String.format("%s field doesn't contain a valid value: %s",
                        RESP_WILL_DEFEND_DIVORCE, defended);
                log.error(String.format("%s. %n Case id: %s.", errorMessage, ccdCallbackRequest.getCaseDetails().getCaseId()));
                throw new WorkflowException(errorMessage);
            }
        }

        return execute(
            tasks.toArray(new Task[tasks.size()]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> caseData) {
        final String respondentSolicitorRepresented = (String) caseData.get(RESP_SOL_REPRESENTED);

        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented);
    }

    private boolean isRespondentSolicitorInformationPresent(Map<String, Object> caseData) {
        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols

        final String respondentSolicitorName = (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME);
        final String respondentSolicitorCompany = (String) caseData.get(D8_RESPONDENT_SOLICITOR_COMPANY);

        return ((respondentSolicitorName != null) && (respondentSolicitorCompany != null));
    }
}