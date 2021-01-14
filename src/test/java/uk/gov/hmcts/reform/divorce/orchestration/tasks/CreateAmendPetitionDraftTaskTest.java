package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class CreateAmendPetitionDraftTaskTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private CreateAmendPetitionDraftTask classUnderTest;

    @Test
    public void executeShouldCallCmsToAmendPetition() {
        TaskContext context = contextWithToken();

        Map<String, Object> payload = new HashMap<>();
        ImmutableMap<String, Object> expectedResult = ImmutableMap.of("field", "value");

        when(caseMaintenanceClient.amendPetition(AUTH_TOKEN)).thenReturn(expectedResult);

        classUnderTest.execute(context, payload);

        assertThat(context.getTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY), is(expectedResult));
    }
}
