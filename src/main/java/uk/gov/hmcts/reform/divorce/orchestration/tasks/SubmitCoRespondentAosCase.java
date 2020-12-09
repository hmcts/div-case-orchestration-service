package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SubmitCoRespondentAosCase implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final Clock clock;

    private static final int DAYS_ALLOWED_FOR_DEFENCE = 21;
    private static final Map<String, String> STATE_TO_SUBMISSION_EVENT_MAP;

    static {
        STATE_TO_SUBMISSION_EVENT_MAP = new HashMap<>();
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_AWAITING, CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_AWAITING_SOLICITOR, CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_STARTED, CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_SUBMITTED_AWAITING_ANSWER, CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_OVERDUE, CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(DEFENDED, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AOS_COMPLETED, CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AWAITING_DECREE_NISI, CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AWAITING_LEGAL_ADVISOR_REFERRAL, CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AWAITING_ALTERNATIVE_SERVICE, CcdEvents.CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AWAITING_PROCESS_SERVER_SERVICE, CcdEvents.CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE);
        STATE_TO_SUBMISSION_EVENT_MAP.put(AWAITING_DWP_RESPONSE, CcdEvents.CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE);
    }

    @Autowired
    public SubmitCoRespondentAosCase(final CaseMaintenanceClient caseMaintenanceClient, final Clock clock) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.clock = clock;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> submissionData) throws TaskException {
        final String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        final CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(authToken);

        if (caseDetails == null) {
            throw new TaskException(new CaseNotFoundException("No case found for user."));
        }

        final String caseState = caseDetails.getState();
        final String eventId = getCoRespondentSubmissionEventForCaseState(caseState, caseDetails.getCaseId());

        final boolean isCoRespondentDefending = submissionData.getOrDefault(CO_RESPONDENT_DEFENDS_DIVORCE, "NO").equals("YES");
        if (isCoRespondentDefending) {
            submissionData.put(CO_RESPONDENT_DUE_DATE, getDueDateForCoRespondent());
        }

        submissionData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);
        submissionData.put(RECEIVED_AOS_FROM_CO_RESP_DATE, LocalDate.now(clock).format(DateUtils.Formatters.CCD_DATE));

        final Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            caseDetails.getCaseId(),
            eventId,
            submissionData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private String getCoRespondentSubmissionEventForCaseState(String state, String caseId) {
        String correspondingEvent = STATE_TO_SUBMISSION_EVENT_MAP.get(state);

        if (correspondingEvent == null) {
            throw new TaskException(new ValidationException(
                String.format("Cannot create co-respondent submission event for case [%s] in state [%s].", caseId,
                    state)));
        }
        return correspondingEvent;
    }

    private String getDueDateForCoRespondent() {
        return LocalDate.now(clock).plusDays(DAYS_ALLOWED_FOR_DEFENCE).format(DateUtils.Formatters.CCD_DATE);
    }
}
