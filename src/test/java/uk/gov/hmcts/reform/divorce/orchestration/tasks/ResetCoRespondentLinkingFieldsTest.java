package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;

public class ResetCoRespondentLinkingFieldsTest {

    private final ResetCoRespondentLinkingFields classUnderTest = new ResetCoRespondentLinkingFields();

    @Test
    public void willClearLinkingData() {
        final Map<String, Object> caseData = new HashMap<>();

        caseData.put(CO_RESP_LINKED_TO_CASE, "foo");
        caseData.put(CO_RESP_LINKED_TO_CASE_DATE, "foo");
        caseData.put(CO_RESP_EMAIL_ADDRESS, "foo");
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, "foo");
        caseData.put(RECEIVED_AOS_FROM_CO_RESP_DATE, "foo");

        final Map<String, Object> result = classUnderTest.execute(new DefaultTaskContext(), caseData);

        assertThat(result, allOf(
            hasEntry(CO_RESP_LINKED_TO_CASE, null),
            hasEntry(CO_RESP_LINKED_TO_CASE_DATE, null),
            hasEntry(CO_RESP_EMAIL_ADDRESS, null),
            hasEntry(RECEIVED_AOS_FROM_CO_RESP, null),
            hasEntry(RECEIVED_AOS_FROM_CO_RESP_DATE, null)
        ));
    }
}
