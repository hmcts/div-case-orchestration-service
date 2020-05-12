package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EventConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.dnReceived;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.dnReceivedAosCompleted;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.submitDnClarification;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Component
public class SubmitDnCase implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    private final EventConfig eventConfig;

    @Autowired
    public SubmitDnCase(final CaseMaintenanceClient caseMaintenanceClient, EventConfig eventConfig) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.eventConfig = eventConfig;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        final CaseDetails currentCaseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        String eventId = getDnEventId(currentCaseDetails);

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

    private String getDnEventId(final CaseDetails currentCaseDetails) {

        final String caseState = currentCaseDetails.getState();

        if (AWAITING_CLARIFICATION.equalsIgnoreCase(caseState)) {
            return  getEventName(currentCaseDetails, submitDnClarification.getEventId());
        } else if (AOS_COMPLETED.equalsIgnoreCase(caseState)) {
            return getEventName(currentCaseDetails, dnReceivedAosCompleted.getEventId());
        } else {
            return getEventName(currentCaseDetails, dnReceived.getEventId());
        }
    }

    private String getEventName(final CaseDetails currentCaseDetails, final String currentEvent) {
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(currentCaseDetails.getCaseData());
        return Optional.ofNullable(eventConfig.getEvents()
                .get(languagePreference.orElse(LanguagePreference.ENGLISH))
                .get(EventType.getEvenType(currentEvent))).orElse(currentEvent);
    }
}
