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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_PARTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class RecordIsUsingOfflineChannelTest {

    @InjectMocks
    private RecordIsUsingOfflineChannel recordIsUsingOfflineChannel;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void whenExecute_thenRespContactMethodIsDigitalIsSetToNo_forRespondent() {
        setPartyToRespondent();
        executeAndExpectValueIsNoFor(RESP_IS_USING_DIGITAL_CHANNEL);
    }

    @Test
    public void whenExecute_thenCoRespContactMethodIsDigitalIsSetToNo_forCoRespondent() {
        setPartyToCoRespondent();
        executeAndExpectValueIsNoFor(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL);
    }


    private void setPartyToRespondent() {
        context.setTransientObject(DIVORCE_PARTY, RESPONDENT);
    }

    private void setPartyToCoRespondent() {
        context.setTransientObject(DIVORCE_PARTY, CO_RESPONDENT);
    }

    private void executeAndExpectValueIsNoFor(String isUsingDigitalChannel) {
        Map<String, Object> returnedPayload = recordIsUsingOfflineChannel.execute(context, testData);
        assertThat(returnedPayload, allOf(
                hasEntry(isUsingDigitalChannel, NO_VALUE)
        ));
    }
}