package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderDraftTaskTest.getDocumentLink;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRefusalDraftRemovalTest {

    @InjectMocks
    private ServiceRefusalDraftRemovalTask classUnderTest;

    @Test
    public void shouldRemoveServiceRefusalDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(SERVICE_REFUSAL_DRAFT, getDocumentLink());

        Map<String, Object> returnedPayload = classUnderTest.execute(prepareTaskContext(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        assertThat(returnedPayload, not(hasKey(SERVICE_REFUSAL_DRAFT)));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove().size(), is(1));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.SERVICE_REFUSAL_DRAFT));
    }
}