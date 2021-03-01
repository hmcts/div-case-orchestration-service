package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.orchestration.util.WelshNextEventUtil;

import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_DN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_DN_RECEIVED_AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_DN_RECEIVED_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_SUBMIT_DN_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.DN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.DN_RECEIVED_AOS_COMPLETE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.DN_RECEIVED_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;

@Component
public class SubmitDnCase implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;
    @Autowired
    private WelshNextEventUtil welshNextEventUtil;

    @Autowired
    public SubmitDnCase(final CaseMaintenanceClient caseMaintenanceClient, final WelshNextEventUtil welshNextEventUtil) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.welshNextEventUtil = welshNextEventUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        final CaseDetails currentCaseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        String eventId = getDnEventId(currentCaseDetails, caseData, context);
        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            caseId,
            eventId,
            caseData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private String getDnEventId(final CaseDetails currentCaseDetails, Map<String, Object> caseData, TaskContext context) {

        final String caseState = currentCaseDetails.getState();

        if (AWAITING_CLARIFICATION.equalsIgnoreCase(caseState)) {
            return evaluateEventId(context, caseData, DN_RECEIVED_CLARIFICATION, BO_WELSH_SUBMIT_DN_CLARIFICATION);
        } else if (AOS_COMPLETED.equalsIgnoreCase(caseState)) {
            return evaluateEventId(context, caseData, DN_RECEIVED_AOS_COMPLETE, BO_WELSH_DN_RECEIVED_AOS_COMPLETED);
        } else {
            return evaluateEventId(context, caseData, DN_RECEIVED, BO_WELSH_DN_RECEIVED);
        }
    }

    private String evaluateEventId(TaskContext context, Map<String, Object> caseData, String eventId, String welshEventId) {
        BooleanSupplier isWelsh = () -> {
            Map<String, Object> currentCasedata =
                Optional.ofNullable(caseData.get(LANGUAGE_PREFERENCE_WELSH)).map(k -> caseData)
                    .orElseGet(() -> caseMaintenanceClient.retrievePetitionById(
                        context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                        context.getTransientObject(CASE_ID_JSON_KEY).toString())
                        .getCaseData());
            return CaseDataUtils.isLanguagePreferenceWelsh(currentCasedata);
        };
        return welshNextEventUtil.storeNextEventAndReturnStopEvent(isWelsh, caseData, eventId, welshEventId, BO_WELSH_DN_RECEIVED_REVIEW);
    }
}
