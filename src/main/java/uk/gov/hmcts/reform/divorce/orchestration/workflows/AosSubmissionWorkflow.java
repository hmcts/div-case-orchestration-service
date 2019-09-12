package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueAosSolicitorSubmitTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_DEFENDING_NOT_ADMITTING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
@RequiredArgsConstructor
public class AosSubmissionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendRespondentSubmissionNotificationForDefendedDivorceEmail
        sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;

    private final SendRespondentSubmissionNotificationForUndefendedDivorceEmail
        sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    private final QueueAosSolicitorSubmitTask
        queueAosSolicitorSubmitTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        List<Task> tasks = new ArrayList<>();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        if (usingRespondentSolicitor(caseData)) {
            tasks.add(queueAosSolicitorSubmitTask);
        } else {
            processCitizenAosSubmissionTasks(ccdCallbackRequest, tasks);
        }

        return execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CCD_CASE_DATA, caseData)
        );
    }

    private void processCitizenAosSubmissionTasks(CcdCallbackRequest ccdCallbackRequest, List<Task> tasks) throws WorkflowException {
        String defended = (String) ccdCallbackRequest.getCaseDetails().getCaseData().get(RESP_WILL_DEFEND_DIVORCE);

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

    private boolean usingRespondentSolicitor(Map<String, Object> caseData) {
        final String respondentSolicitorRepresented = (String) caseData.get(RESP_SOL_REPRESENTED);

        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols
        final String respondentSolicitorName = (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME);
        final String respondentSolicitorCompany = (String) caseData.get(D8_RESPONDENT_SOLICITOR_COMPANY);

        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented)
            || respondentSolicitorName != null && respondentSolicitorCompany != null;
    }
}