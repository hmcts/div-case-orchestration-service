package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

public class ResetRespondentLinkingFieldsTest {

    private final ResetRespondentLinkingFields classUnderTest = new ResetRespondentLinkingFields();

    @Test
    public void willClearLinkingData() {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(RECEIVED_AOS_FROM_RESP, "foo");
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, "foo");
        caseData.put(RESPONDENT_EMAIL_ADDRESS, "foo");


        final Map<String, Object> result = classUnderTest.execute(new DefaultTaskContext(), caseData);

        assertThat(result, allOf(
            hasEntry(RECEIVED_AOS_FROM_RESP, null),
            hasEntry(RECEIVED_AOS_FROM_RESP_DATE, null),
            hasEntry(RESPONDENT_EMAIL_ADDRESS, null)
        ));
    }
}
