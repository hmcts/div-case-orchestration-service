package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.StrategicIdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@AllArgsConstructor
@Component
public class ValidateExistingSolicitorLink implements Task<UserDetails> {

    private final StrategicIdamClient idamClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        String existingSolicitorEmail = (String) caseDetails.getCaseData().get(SOLICITOR_LINKED_EMAIL);
        UserDetails solicitorDetails = idamClient.retrieveUserDetails(context.getTransientObject(AUTH_TOKEN_JSON_KEY));
        context.setTransientObject(SOLICITOR_LINKED_EMAIL, solicitorDetails.getEmail());
        if (!Strings.isNullOrEmpty(existingSolicitorEmail)) {
            throw new TaskException(String.format("Case is already linked - ID %s", caseDetails.getCaseId()));
        }
        return payload;
    }
}
