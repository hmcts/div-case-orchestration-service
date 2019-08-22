package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTaskTest.matchesDocumentInputParameters;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiRefusalDocumentGeneratorTaskTest {

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DecreeNisiRefusalDocumentGeneratorTask decreeNisiRefusalDocumentGeneratorTask;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentForDnRefusalClarification() {
        final Map<String, Object> payload = new HashMap<>();
        payload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
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
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(true);
        when(documentGeneratorClient
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN))
        ).thenReturn(expectedDocument);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        verify(documentGeneratorClient)
                .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void doesNotCallDocumentGeneratorWhenRefusalReasonIsNotMoreInfo() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(payload)
                .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        //given
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(true);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, isNull());

        verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        verify(documentGeneratorClient, never())
                .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void doesNotCallDocumentGeneratorWhenFeatureToggleIsOff() {
        final Map<String, Object> payload = new HashMap<>();
        payload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(payload)
                .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        //given
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, isNull());

        verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        verify(documentGeneratorClient, never())
                .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN));
    }
}