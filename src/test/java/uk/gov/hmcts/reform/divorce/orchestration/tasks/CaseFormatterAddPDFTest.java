package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DOC_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class CaseFormatterAddPDFTest {
    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private CaseFormatterAddPDF caseFormatterAddPDF;

    @Test
    public void givenBothDocumentsExist_whenExecute_thenReturnExpected() {
        final GeneratedDocumentInfo petition = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final GeneratedDocumentInfo aosInvitation = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final Map<String, Object> payload = new HashMap<>();
        final TaskContext context = new DefaultTaskContext();
        final  DocumentUpdateRequest documentUpdateRequest =
            DocumentUpdateRequest.builder()
                .caseData(payload)
                .documents(ImmutableList.of(petition, aosInvitation))
            .build();

        context.setTransientObject(MINI_PETITION_TEMPLATE_NAME, petition);
        context.setTransientObject(RESPONDENT_INVITATION_TEMPLATE_NAME, aosInvitation);

        //given
        when(caseFormatterClient.addDocuments(documentUpdateRequest)).thenReturn(payload);

        //when
        Map<String, Object> response = caseFormatterAddPDF.execute(context, payload);

        assertEquals(payload, response);

        verify(caseFormatterClient).addDocuments(documentUpdateRequest);
    }

    @Test
    public void givenOnlyMiniPetitionExists_whenExecute_thenReturnExpected() {
        final GeneratedDocumentInfo petition = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final Map<String, Object> payload = new HashMap<>();
        final TaskContext context = new DefaultTaskContext();
        final  DocumentUpdateRequest documentUpdateRequest =
            DocumentUpdateRequest.builder()
                .caseData(payload)
                .documents(Collections.singletonList(petition))
                .build();

        context.setTransientObject(MINI_PETITION_TEMPLATE_NAME, petition);

        //given
        when(caseFormatterClient.addDocuments(documentUpdateRequest)).thenReturn(payload);

        //when
        Map<String, Object> response = caseFormatterAddPDF.execute(context, payload);

        assertEquals(payload, response);

        verify(caseFormatterClient).addDocuments(documentUpdateRequest);
    }
}