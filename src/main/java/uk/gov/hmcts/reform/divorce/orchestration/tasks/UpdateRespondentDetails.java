package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_START_FROM_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_START_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.LINK_RESPONDENT_GENERIC;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.START_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class UpdateRespondentDetails implements Task<UserDetails> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final IdamClient idamClient;
    private final AuthUtil authUtil;
    private final CcdUtil ccdUtil;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {

        Map<String, Object> updateFields = new HashMap<>();
        try {
            boolean isRespondent = context.getTransientObject(IS_RESPONDENT);
            String eventId;

            UserDetails linkedUser =
                idamClient.getUserDetails(
                    authUtil.getBearerToken(context.getTransientObject(AUTH_TOKEN_JSON_KEY)));

            CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

            if (isRespondent) {
                updateFields.put(RESPONDENT_EMAIL_ADDRESS, linkedUser.getEmail());
                eventId = getEventId(caseDetails.getState());
            } else {
                updateFields.put(CO_RESP_EMAIL_ADDRESS, linkedUser.getEmail());
                updateFields.put(CO_RESP_LINKED_TO_CASE, YES_VALUE);
                updateFields.put(CO_RESP_LINKED_TO_CASE_DATE, ccdUtil.getCurrentDateCcdFormat());
                eventId = LINK_RESPONDENT_GENERIC;
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
            case AWAITING_ALTERNATIVE_SERVICE:
            case AWAITING_PROCESS_SERVER_SERVICE:
            case AWAITING_DWP_RESPONSE:
            case AWAITING_BAILIFF_REFERRAL:
            case AWAITING_BAILIFF_SERVICE:
            case ISSUED_TO_BAILIFF:
                return START_AOS;
            case AOS_OVERDUE:
                return AOS_START_FROM_OVERDUE;
            case AWAITING_REISSUE:
                return AOS_START_FROM_REISSUE;
            case SERVICE_APPLICATION_NOT_APPROVED:
                return AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED;
            default:
                return LINK_RESPONDENT_GENERIC;
        }
    }
}
