package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class CaseFormatterAddDocumentsTest {
    @Mock
    private CaseFormatterService caseFormatterService;

    @InjectMocks
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Test
    public void passesAllDocumentsInOrderToFormatter() {
        final GeneratedDocumentInfo petition = GeneratedDocumentInfo.builder()
            .fileName(randomUUID().toString())
            .build();

        final GeneratedDocumentInfo aosInvitation = GeneratedDocumentInfo.builder()
            .fileName(randomUUID().toString())
            .build();

        final GeneratedDocumentInfo coRespondentInvitation = GeneratedDocumentInfo.builder()
            .fileName(randomUUID().toString())
            .build();

        final Map<String, Object> inboundPayload = new HashMap<>();
        final DefaultTaskContext context = new DefaultTaskContext();

        final LinkedHashSet<GeneratedDocumentInfo> allDocuments = new LinkedHashSet<GeneratedDocumentInfo>() {
            {
                add(petition);
                add(aosInvitation);
                add(coRespondentInvitation);
            }
        };
        context.setTransientObject(DOCUMENT_COLLECTION, allDocuments);

        List<uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo> collect =
                allDocuments.stream().map(generatedDocumentInfo -> {
                    uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo generatedDocumentModel =
                            new uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo();
                    generatedDocumentModel.setFileName(generatedDocumentInfo.getFileName());
                    generatedDocumentModel.setDocumentType(generatedDocumentInfo.getDocumentType());
                    generatedDocumentModel.setUrl(generatedDocumentInfo.getUrl());
                    return generatedDocumentModel;
                }).collect(Collectors.toList());

        final Map<String, Object> payloadWithDocumentsAttached = new HashMap<>();
        when(caseFormatterService.addDocuments(inboundPayload, collect))
                .thenReturn(payloadWithDocumentsAttached);

        Map<String, Object> response = caseFormatterAddDocuments.execute(context, inboundPayload);

        assertThat(response, is(payloadWithDocumentsAttached));

        verify(caseFormatterService).addDocuments(inboundPayload, collect);
    }

    @Test
    public void formatterNotCalledIfDocumentCollectionIsNull() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_COLLECTION, null);

        final Map<String, Object> payload = new HashMap<>();
        Map<String, Object> response = caseFormatterAddDocuments.execute(context, payload);

        assertThat(response, is(payload));

        verifyZeroInteractions(caseFormatterService);
    }

    @Test
    public void formatterNotCalledIfDocumentCollectionIsEmpty() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_COLLECTION, emptySet());

        final Map<String, Object> payload = new HashMap<>();
        Map<String, Object> response = caseFormatterAddDocuments.execute(context, payload);

        assertThat(response, is(payload));

        verifyZeroInteractions(caseFormatterService);
    }
}
