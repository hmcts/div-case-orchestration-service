package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;

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

    private DocumentLink getDocumentLink() {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl("binary_url");
        documentLink.setDocumentFilename("file_name");
        documentLink.setDocumentUrl("url");
        return documentLink;
    }

}