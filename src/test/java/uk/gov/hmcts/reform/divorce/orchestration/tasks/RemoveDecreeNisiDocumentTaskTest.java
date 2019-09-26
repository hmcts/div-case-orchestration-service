package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;

public class RemoveDecreeNisiDocumentTaskTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private RemoveDecreeNisiDocumentTask classToTest;

    @Test
    public void callsRemoveMiniPetitionDraftDocumentsTaskAndDeletesPetitionFromDocumentCollection() {
        final Map<String, Object> payload = new HashMap<>();
        final Map<String, Object> expected = DUMMY_CASE_DATA;

        when(caseFormatterClient.removeAllDocumentsByType(DECREE_NISI_DOCUMENT_TYPE, payload)).thenReturn(expected);

        assertThat(classToTest.execute(new DefaultTaskContext(), payload), Is.is(expected));
    }
}
