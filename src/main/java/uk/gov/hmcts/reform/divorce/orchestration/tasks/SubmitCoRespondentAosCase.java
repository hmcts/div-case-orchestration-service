package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SubmitCoRespondentAosCase implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final Clock clock;

    private static final int DAYS_ALLOWED_FOR_DEFENCE = 21;

    @Autowired
    public SubmitCoRespondentAosCase(final CaseMaintenanceClient caseMaintenanceClient, final Clock clock) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.clock = clock;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> submissionData) throws TaskException {
        final String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        final CaseDetails currentCaseDetails = caseMaintenanceClient.retrieveAosCase(authToken);

        if (currentCaseDetails == null) {
            throw new TaskException(new CaseNotFoundException("No case found for user."));
        }

        final String currentCaseState = currentCaseDetails.getState();
        final String eventId = getCoRespondentSubmissionEventForCaseState(currentCaseState);

        if (eventId == null) {
            throw new TaskException(new ValidationException(
                String.format("Cannot create co-respondent submission event for case [%s] in state [%s].", currentCaseDetails.getCaseId(),
                    currentCaseState)));
        }

        final boolean isCoRespondentDefending = submissionData.getOrDefault(CO_RESPONDENT_DEFENDS_DIVORCE, "NO").equals("YES");
        if (isCoRespondentDefending) {
            submissionData.put(CO_RESPONDENT_DUE_DATE, getDueDateForCoRespondent());
        }

        submissionData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);
        submissionData.put(RECEIVED_AOS_FROM_CO_RESP_DATE, LocalDate.now(clock).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT)));

        final Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            currentCaseDetails.getCaseId(),
            eventId,
            submissionData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private String getCoRespondentSubmissionEventForCaseState(final String currentCaseState) {
        if (AOS_AWAITING.equals(currentCaseState)) {
            // Co-respondent can respond even if the respondent has not yet responded.
            return CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID;
        }
        if (AOS_STARTED.equals(currentCaseState)) {
            // Co-respondent can respond at the same time the respondent has responded.
            return CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID;
        }
        if (AOS_SUBMITTED_AWAITING_ANSWER.equals(currentCaseState)) {
            // The Respondent is intending to defend (having actually submitted their AOS), so it is perfectly acceptable
            // for the co-respondent to respond also (even if Co-resp is NOT defending)
            return CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID;
        }
        if (AOS_OVERDUE.equals(currentCaseState)) {
            // Co-respondent can respond even if the respondent is late to respond.
            return CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID;
        }
        if (DEFENDED.equals(currentCaseState)) {
            // The Respondent did their online AOS and their paper defence; case will now go to court hearing.
            // But the co-respondent can still reply as their reply may be relevant in the court hearing.
            return CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID;
        }
        if (AOS_COMPLETED.equals(currentCaseState)) {
            return CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID;
        }
        if (AWAITING_DECREE_NISI.equals(currentCaseState) || DN_AWAITING.equals(currentCaseState)) {
            return CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID;
        }
        if (AWAITING_LEGAL_ADVISOR_REFERRAL.equals(currentCaseState)) {
            return CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID;
        }

        return null;
    }

    private String getDueDateForCoRespondent() {
        return LocalDate.now(clock).plusDays(DAYS_ALLOWED_FOR_DEFENCE).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT));
    }
}
