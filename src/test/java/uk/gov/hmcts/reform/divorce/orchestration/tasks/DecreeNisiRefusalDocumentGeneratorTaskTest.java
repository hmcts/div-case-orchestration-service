package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_EXTENSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_LINK_FILENAME_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_LINK_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTaskTest.matchesDocumentInputParameters;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiRefusalDocumentGeneratorTaskTest {

    private static final String FIXED_DATE = "2010-10-10";

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private DecreeNisiRefusalDocumentGeneratorTask decreeNisiRefusalDocumentGeneratorTask;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentForDnRefusalClarificationWithMoreInfoRequested() {
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
            .fileName(DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME + TEST_CASE_ID)
            .build();

        //given
        when(documentGeneratorClient
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                caseDetails), eq(AUTH_TOKEN))
        ).thenReturn(expectedDocument);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(documentGeneratorClient)
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                caseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void callsDocumentGeneratorAndStoresAdditionalGeneratedDocumentForDnRefusalClarificationWithExistingDocs() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);

        Map<String, Object> document = new HashMap<>();
        document.put(DOCUMENT_TYPE_JSON_KEY, DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE);
        document.put(DOCUMENT_FILENAME_JSON_KEY, DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME);
        document.put(DOCUMENT_LINK_JSON_KEY, new HashMap<String, Object>() {
            {
                put(DOCUMENT_LINK_FILENAME_JSON_KEY,DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME + DOCUMENT_EXTENSION);
            }
        });

        Map<String, Object> documentMember = new HashMap<>();
        documentMember.put(VALUE_KEY, document);

        List<Map<String, Object>> existingDocuments = new ArrayList<>();
        existingDocuments.add(documentMember);

        final Map<String, Object> payload = new HashMap<>();
        payload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        payload.put(D8DOCUMENTS_GENERATED, existingDocuments);
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
            .fileName(DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME + TEST_CASE_ID)
            .build();

        //given
        when(documentGeneratorClient
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                caseDetails), eq(AUTH_TOKEN))
        ).thenReturn(expectedDocument);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        List<Map<String, Object>> currentGeneratedDocs =
            (List<Map<String, Object>>) caseDetails.getCaseData().get(D8DOCUMENTS_GENERATED);
        Map<String, Object> initialDocument = (Map<String, Object>) currentGeneratedDocs.get(0).get(VALUE_KEY);

        assertThat(initialDocument.get(DOCUMENT_TYPE_JSON_KEY), is(DOCUMENT_TYPE_OTHER));
        assertThat(initialDocument.get(DOCUMENT_FILENAME_JSON_KEY),
            is(DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD + TEST_CASE_ID + "-" + FIXED_TIME_EPOCH));

            is(DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD + FIXED_DATE));
        assertThat(initialDocument.get(DOCUMENT_LINK_JSON_KEY),
            is(new HashMap<String, Object>() {
                {
                    put(DOCUMENT_LINK_FILENAME_JSON_KEY,
                            DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD + FIXED_DATE + DOCUMENT_EXTENSION);
                }
            })
        );

        verify(documentGeneratorClient)
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                caseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentForDnRefusalRejectionWithRejectOption() {
        final FeeResponse amendFee = FeeResponse.builder().amount(TEST_FEE_AMOUNT).build();

        final Map<String, Object> payload = new HashMap<>();
        payload.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(AMEND_PETITION_FEE_JSON_KEY, amendFee);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo expectedDocument = GeneratedDocumentInfo.builder()
            .documentType(DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE)
            .fileName(DECREE_NISI_REFUSAL_DOCUMENT_NAME + TEST_CASE_ID)
            .build();

        final Map<String, Object> expectedPayload = new HashMap<>(payload);
        expectedPayload.put(FEE_TO_PAY_JSON_KEY, amendFee.getFormattedFeeAmount());

        final CaseDetails expectedCaseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(expectedPayload)
            .build();

        //given
        when(documentGeneratorClient
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID, expectedCaseDetails), eq(AUTH_TOKEN))
        ).thenReturn(expectedDocument);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(documentGeneratorClient)
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID, expectedCaseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void returnsCaseDataWhenNothingRequested() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        //when
        decreeNisiRefusalDocumentGeneratorTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertEquals(documentCollection.size(), 0);

        verify(documentGeneratorClient, never())
            .generatePDF(matchesDocumentInputParameters(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                caseDetails), eq(AUTH_TOKEN));
    }
}
