package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ValidateExistingSolicitorLinkTest {

    @InjectMocks
    private ValidateExistingSolicitorLink validateCaseData;

    public void doesNothingIfSolicitorLinkedFieldIsNotSet() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        String solicitorEmail = "test@sol.local";
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        UserDetails build = UserDetails.builder().email(solicitorEmail).build();
        validateCaseData.execute(context, build);
    }

    public void doesNothingIfSolicitorLinkedFieldIfEmailsMatch() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        String solicitorEmail = "test@sol.local";
        CaseDetails caseDetails = CaseDetails.builder().caseData(
                Collections.singletonMap(SOLICITOR_LINKED_EMAIL, solicitorEmail)
        ).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        UserDetails build = UserDetails.builder().email(solicitorEmail).build();
        validateCaseData.execute(context, build);
    }

    @Test(expected = TaskException.class)
    public void throwExceptionIfCaseSolicitorLinkFieldIsDifferentFromExistingValue() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        CaseDetails caseDetails = CaseDetails.builder().caseData(
                Collections.singletonMap(SOLICITOR_LINKED_EMAIL, "test@sol.local")
        ).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        UserDetails build = UserDetails.builder().email("test1@sol.local").build();
        validateCaseData.execute(context, build);
    }
}
