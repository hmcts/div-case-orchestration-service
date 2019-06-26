package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@Component
public class ValidateExistingSolicitorLink implements Task<UserDetails> {

    @Override
    public UserDetails execute(TaskContext context, UserDetails solicitorDetails) throws TaskException {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        String existingSolicitorEmail = (String) caseDetails.getCaseData().get(SOLICITOR_LINKED_EMAIL);

        if (!Strings.isNullOrEmpty(existingSolicitorEmail)) {
            if (!solicitorDetails.getEmail().equals(existingSolicitorEmail)) {
                throw new TaskException(String.format("Case %s is already linked", caseDetails.getCaseId()));
            }
        }
        return solicitorDetails;
    }
}
