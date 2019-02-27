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

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DOC_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_INVITATION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class CaseFormatterAddDocumentsTest {
    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Test
    public void givenAllDocumentsExist_whenExecute_thenReturnExpected() {
        final GeneratedDocumentInfo petition = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final GeneratedDocumentInfo aosInvitation = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final GeneratedDocumentInfo coRespondentInvitation = GeneratedDocumentInfo.builder()
            .fileName(TEST_FILENAME)
            .url(TEST_DOC_URL)
            .build();

        final Map<String, Object> payload = new HashMap<>();
        final TaskContext context = new DefaultTaskContext();
        final  DocumentUpdateRequest documentUpdateRequest =
            DocumentUpdateRequest.builder()
                .caseData(payload)
                .documents(ImmutableList.of(petition, aosInvitation, coRespondentInvitation))
            .build();

        context.setTransientObject(MINI_PETITION_TEMPLATE_NAME, petition);
        context.setTransientObject(RESPONDENT_INVITATION_TEMPLATE_NAME, aosInvitation);
        context.setTransientObject(CO_RESPONDENT_INVITATION_TEMPLATE_NAME, coRespondentInvitation);

        //given
        when(caseFormatterClient.addDocuments(documentUpdateRequest)).thenReturn(payload);

        //when
        Map<String, Object> response = caseFormatterAddDocuments.execute(context, payload);

        assertThat(response, is(payload));

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
                .documents(singletonList(petition))
                .build();

        context.setTransientObject(MINI_PETITION_TEMPLATE_NAME, petition);

        //given
        when(caseFormatterClient.addDocuments(documentUpdateRequest)).thenReturn(payload);

        //when
        Map<String, Object> response = caseFormatterAddDocuments.execute(context, payload);

        assertThat(response, is(payload));

        verify(caseFormatterClient).addDocuments(documentUpdateRequest);
    }

    @Test
    public void givenAOSExistsWithoutCoRespondent_whenExecute_thenReturnExpected() {
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
        Map<String, Object> response = caseFormatterAddDocuments.execute(context, payload);

        assertThat(response, is(payload));

        verify(caseFormatterClient).addDocuments(documentUpdateRequest);
    }
}
