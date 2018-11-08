package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

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
        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                Boolean.valueOf(String.valueOf(context.getTransientObject(CHECK_CCD))));

        String event;
        if (caseDetails.getState().equals("AosAwaiting") || caseDetails.getState().equals("AosOverdue")
                || caseDetails.getState().equals("AwaitingReissue")) {
            event = START_AOS_EVENT_ID;
        } else {
            event = RESPONDENT_LINK_GENERIC_EVENT_ID;
        }
        UserDetails respondentDetails =
            idamClient.retrieveUserDetails(
                AuthUtil.getBearToken((String)context.getTransientObject(AUTH_TOKEN_JSON_KEY)));

        caseMaintenanceClient.updateCase(
            (String)context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            (String)context.getTransientObject(CASE_ID_JSON_KEY),
            event,
            ImmutableMap.of(
                RESPONDENT_EMAIL_ADDRESS, respondentDetails.getEmail(),
                RECEIVED_AOS_FROM_RESP, YES_VALUE,
                RECEIVED_AOS_FROM_RESP_DATE, CcdUtil.getCurrentDate(),
                CCD_DUE_DATE, CcdUtil.getCurrentDatePlusDays(daysToComplete)
            )
        );

        return payLoad;
    }
}
