package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SetSolicitorLinkedFieldTest {

    private static final String RESP_SOLICITOR_LINKED_EMAIL = "RespSolLinkedEmail";
    private static final String SOL_EMAIL = "sol@test.local";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SetSolicitorLinkedField setSolicitorLinkedField;

    @Test
    public void whenTaskIsCalled_setSolicitorLinkedEmailField() throws TaskException {

        final UserDetails userDetails = UserDetails.builder().build();

        final Map<String, Object> dataToUpdate =
                ImmutableMap.of(
                        RESP_SOLICITOR_LINKED_EMAIL, SOL_EMAIL
                );

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(SOLICITOR_LINKED_EMAIL, SOL_EMAIL);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, LINK_RESPONDENT_GENERIC_EVENT_ID, dataToUpdate))
                .thenReturn(null);

        assertThat(userDetails, is(setSolicitorLinkedField.execute(taskContext, userDetails)));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, LINK_RESPONDENT_GENERIC_EVENT_ID, dataToUpdate);
    }
}