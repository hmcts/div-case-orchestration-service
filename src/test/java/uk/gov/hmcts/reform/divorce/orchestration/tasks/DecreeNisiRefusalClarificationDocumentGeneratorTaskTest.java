package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTaskTest.matchesDocumentInputParameters;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiRefusalClarificationDocumentGeneratorTaskTest {

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private DecreeNisiRefusalClarificationDocumentGeneratorTask decreeNisiRefusalClarificationDocumentGeneratorTask;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentForDnRefusalClarification() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(payload)
                .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo expectedDocument = GeneratedDocumentInfo.builder()
                .documentType(DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE)
                .fileName(DECREE_NISI_REFUSAL_DOCUMENT_NAME + TEST_CASE_ID)
                .build();

        //given
        when(documentGeneratorClient
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN))
        ).thenReturn(expectedDocument);

        //when
        decreeNisiRefusalClarificationDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(documentGeneratorClient)
                .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN));
    }
}