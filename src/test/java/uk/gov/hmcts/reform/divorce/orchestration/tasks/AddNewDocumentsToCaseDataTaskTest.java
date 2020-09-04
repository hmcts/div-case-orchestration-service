package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class AddNewDocumentsToCaseDataTaskTest {

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

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

        final LinkedHashSet<GeneratedDocumentInfo> allDocuments = new LinkedHashSet<>();
        allDocuments.add(petition);
        allDocuments.add(aosInvitation);
        allDocuments.add(coRespondentInvitation);

        context.setTransientObject(DOCUMENT_COLLECTION, allDocuments);

        List<GeneratedDocumentInfo> generatedDocumentInfoList = ImmutableList.of(petition, aosInvitation, coRespondentInvitation);
        final Map<String, Object> payloadWithDocumentsAttached = new HashMap<>();
        when(ccdUtil.addNewDocumentsToCaseData(inboundPayload, generatedDocumentInfoList)).thenReturn(payloadWithDocumentsAttached);

        Map<String, Object> response = addNewDocumentsToCaseDataTask.execute(context, inboundPayload);

        assertThat(response, is(payloadWithDocumentsAttached));
        verify(ccdUtil).addNewDocumentsToCaseData(inboundPayload, generatedDocumentInfoList);
    }

    @Test
    public void formatterNotCalledIfDocumentCollectionIsNull() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_COLLECTION, null);

        final Map<String, Object> payload = new HashMap<>();
        Map<String, Object> response = addNewDocumentsToCaseDataTask.execute(context, payload);

        assertThat(response, is(payload));

        verifyNoInteractions(ccdUtil);
    }

    @Test
    public void formatterNotCalledIfDocumentCollectionIsEmpty() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_COLLECTION, emptySet());

        final Map<String, Object> payload = new HashMap<>();
        Map<String, Object> response = addNewDocumentsToCaseDataTask.execute(context, payload);

        assertThat(response, is(payload));

        verifyNoInteractions(ccdUtil);
    }

}