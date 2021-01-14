package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.DIVORCE_PARTY;

@RunWith(MockitoJUnitRunner.class)
public class MarkJourneyAsOfflineTaskTest {

    @InjectMocks
    private MarkJourneyAsOfflineTask markJourneyAsOfflineTask;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void whenExecute_thenRespContactMethodIsDigitalIsSetToNo_forRespondent() {
        context.setTransientObject(DIVORCE_PARTY, RESPONDENT);

        Map<String, Object> returnedPayload = markJourneyAsOfflineTask.execute(context, testData);
        assertThat(returnedPayload, allOf(
                hasEntry(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
        ));
    }

    @Test
    public void whenExecute_thenCoRespContactMethodIsDigitalIsSetToNo_forCoRespondent() {
        context.setTransientObject(DIVORCE_PARTY, CO_RESPONDENT);

        Map<String, Object> returnedPayload = markJourneyAsOfflineTask.execute(context, testData);
        assertThat(returnedPayload, allOf(
                hasEntry(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
        ));
    }
}