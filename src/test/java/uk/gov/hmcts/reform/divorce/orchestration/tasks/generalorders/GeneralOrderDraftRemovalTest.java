package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderDraftTaskTest.getDocumentLink;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderDraftRemovalTest {

    @InjectMocks
    private GeneralOrderDraftRemovalTask classUnderTest;

    @Test
    public void shouldRemoveGeneralOrderDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(classUnderTest.getFieldToRemove(), getDocumentLink());

        Map<String, Object> returnedPayload = classUnderTest.execute(prepareTaskContext(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        assertThat(returnedPayload, not(hasKey(classUnderTest.getFieldToRemove())));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldToRemove(), is(CcdFields.GENERAL_ORDER_DRAFT));
    }
}