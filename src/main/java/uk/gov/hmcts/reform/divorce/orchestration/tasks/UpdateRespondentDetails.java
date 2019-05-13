package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_START_FROM_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_START_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.START_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class UpdateRespondentDetails implements Task<UserDetails> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    @Qualifier("idamClient")
    private IdamClient idamClient;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CcdUtil ccdUtil;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {

        Map<String, Object> updateFields = new HashMap<>();
        try {
            boolean isRespondent = context.getTransientObject(IS_RESPONDENT);
            String eventId;

            UserDetails linkedUser =
                idamClient.retrieveUserDetails(
                    authUtil.getBearToken(context.getTransientObject(AUTH_TOKEN_JSON_KEY)));

            CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

            if (isRespondent) {
                updateFields.put(RESPONDENT_EMAIL_ADDRESS, linkedUser.getEmail());
                eventId = getEventId(caseDetails.getState());
            } else {
                updateFields.put(CO_RESP_EMAIL_ADDRESS, linkedUser.getEmail());
                updateFields.put(CO_RESP_LINKED_TO_CASE, YES_VALUE);
                updateFields.put(CO_RESP_LINKED_TO_CASE_DATE, ccdUtil.getCurrentDateCcdFormat());
                eventId = LINK_RESPONDENT_GENERIC_EVENT_ID;
            }

            caseMaintenanceClient.updateCase(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                context.getTransientObject(CASE_ID_JSON_KEY),
                eventId,
                updateFields
            );
        } catch (FeignException ex) {
            context.setTransientObject(UPDATE_RESPONDENT_DATA_ERROR_KEY, payload);
            throw new TaskException("Case update failed", ex);
        }

        return payload;
    }

    private String getEventId(String state) {

        switch (state) {
            case AOS_AWAITING:
                return START_AOS_EVENT_ID;
            case AOS_OVERDUE:
                return AOS_START_FROM_OVERDUE;
            case AWAITING_REISSUE:
                return AOS_START_FROM_REISSUE;
            default:
                return LINK_RESPONDENT_GENERIC_EVENT_ID;
        }
    }
}
