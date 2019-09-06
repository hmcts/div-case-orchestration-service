package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoveMiniPetitionDraftDocumentsTaskTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @InjectMocks
    private RemoveMiniPetitionDraftDocumentsTask removeMiniPetitionDraftDocumentsTask;

    @Test
    public void callsRemoveMiniPetitionDraftDocumentsTaskAndDeletesPetitionFromDocumentCollection() {
        final Map<String, Object> payload = new HashMap<>();

        //given
        when(caseFormatterService.removeAllPetitionDocuments(payload)).thenReturn(payload);

        //when
        removeMiniPetitionDraftDocumentsTask.execute(new DefaultTaskContext(), payload);

        verify(caseFormatterService).removeAllPetitionDocuments(payload);
    }
}
