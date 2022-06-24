package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class MarkRespondentAsNonDigitalTaskTest {

    private MarkRespondentAsNonDigitalTask markRespondentAsNonDigital = new MarkRespondentAsNonDigitalTask();

    @Test
    public void executeShouldSetRespondentAsNonDigital() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = markRespondentAsNonDigital
            .execute(contextWithToken(), caseData);
        assertThat(returnedCaseData.isEmpty(), is(false));
        String value = (String) returnedCaseData.get(OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL);
        assertThat(value, is(OrchestrationConstants.NO_VALUE));
    }

    @Test
    public void executeShouldNotSetRespondentAsNonDigitalIfAlreadySet() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL, OrchestrationConstants.YES_VALUE);
        Map<String, Object> returnedCaseData = markRespondentAsNonDigital
            .execute(contextWithToken(), caseData);
        assertThat(returnedCaseData.isEmpty(), is(false));
        String value = (String) returnedCaseData.get(OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL);
        assertThat(value, is(OrchestrationConstants.YES_VALUE));
    }
}
