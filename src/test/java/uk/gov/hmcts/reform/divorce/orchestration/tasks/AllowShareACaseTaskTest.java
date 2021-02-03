package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithCommonValues;

@RunWith(MockitoJUnitRunner.class)
public class AllowShareACaseTaskTest {

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @InjectMocks
    private AllowShareACaseTask allowShareACaseTask;

    @Test
    public void shouldRemoveCaseRoleAndAssignAccessToCase() {
        Map<String, Object> input = new HashMap<>();
        TaskContext context = contextWithCommonValues();

        Map<String, Object> result = allowShareACaseTask.execute(contextWithCommonValues(), new HashMap<>());

        assertThat(result, is(input));
        verify(assignCaseAccessService)
            .assignCaseAccess(
                context.getTransientObject(CASE_DETAILS_JSON_KEY),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );
        verify(ccdDataStoreService)
            .removeCreatorRole(
                context.getTransientObject(CASE_DETAILS_JSON_KEY),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );
    }
}
