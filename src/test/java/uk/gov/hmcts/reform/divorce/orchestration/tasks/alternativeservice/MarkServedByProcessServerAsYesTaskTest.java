package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class MarkServedByProcessServerAsYesTaskTest {

    @InjectMocks
    private MarkServedByProcessServerAsYesTask markServedByProcessServerAsYesTask;

    @Test
    public void whenMarkServedByProcessServerAsYesTaskIsExecute_thenServedByProcessServerIsSetToYes() {
        Map<String, Object> returnedPayload = markServedByProcessServerAsYesTask.execute(context(), new HashMap<>());

        assertThat(returnedPayload, hasEntry(CcdFields.SERVED_BY_PROCESS_SERVER, YES_VALUE));
    }
}