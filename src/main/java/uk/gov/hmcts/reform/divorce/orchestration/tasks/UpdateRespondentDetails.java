package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_START_FROM_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.START_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class UpdateRespondentDetails implements Task<UserDetails> {

    @Value("${aos.responded.days-to-complete}")
    private int daysToComplete;

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    @Qualifier("idamClient")
    private IdamClient idamClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payLoad) {

        Map<String, Object> updateFields = new HashMap<>();
        UserDetails respondentDetails =
            idamClient.retrieveUserDetails(
                AuthUtil.getBearToken((String)context.getTransientObject(AUTH_TOKEN_JSON_KEY)));

        updateFields.put(RESPONDENT_EMAIL_ADDRESS, respondentDetails.getEmail());
        updateFields.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        updateFields.put(RECEIVED_AOS_FROM_RESP_DATE, CcdUtil.getCurrentDate());

        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                true);

        String eventId = getEventId(caseDetails.getState());

        boolean standardAosFlow = START_AOS_EVENT_ID.equals(eventId)
                || AOS_START_FROM_OVERDUE.equals(eventId);

        if (standardAosFlow) {
            updateFields.put(CCD_DUE_DATE, CcdUtil.getCurrentDatePlusDays(daysToComplete));
        }

        caseMaintenanceClient.updateCase(
            (String)context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            (String)context.getTransientObject(CASE_ID_JSON_KEY),
            eventId,
            updateFields
        );

        return payLoad;
    }

    private String getEventId(String state) {

        switch (state) {
            case AOS_AWAITING:
                return START_AOS_EVENT_ID;
            case AOS_OVERDUE:
                return AOS_START_FROM_OVERDUE;
            default:
                return LINK_RESPONDENT_GENERIC_EVENT_ID;
        }
    }


}
