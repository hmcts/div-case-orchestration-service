package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COURT_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTaskTest.matchesDocumentInputParameters;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationTaskTest {

    private static final String TEST_DOCUMENT_TEMPLATE_ID = "a";
    private static final String TEST_DOCUMENT_TYPE = "b";
    private static final String TEST_DOCUMENT_FILE_NAME = "c";

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private DocumentGenerationTask documentGenerationTask;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, TEST_DOCUMENT_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, TEST_DOCUMENT_TYPE);
        context.setTransientObject(DOCUMENT_FILENAME, TEST_DOCUMENT_FILE_NAME);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo expectedDocument = GeneratedDocumentInfo.builder()
            .documentType(TEST_DOCUMENT_TYPE)
            .fileName(TEST_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        //given
        when(documentGeneratorClient.generatePDF(matchesDocumentInputParameters(TEST_DOCUMENT_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN)))
            .thenReturn(expectedDocument);

        //when
        documentGenerationTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(documentGeneratorClient).generatePDF(matchesDocumentInputParameters(TEST_DOCUMENT_TEMPLATE_ID, caseDetails), eq(AUTH_TOKEN));
    }

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentWithDnCourtDetails() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, TEST_DOCUMENT_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, TEST_DOCUMENT_TYPE);
        context.setTransientObject(DOCUMENT_FILENAME, TEST_DOCUMENT_FILE_NAME);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        Map<String, Object> dnCourtDetails = ImmutableMap.of(
            COURT_NAME_CCD_FIELD, "TestCourt",
            COURT_CONTACT_JSON_KEY, "TestContact"
        );
        context.setTransientObject(DN_COURT_DETAILS, dnCourtDetails);

        final GeneratedDocumentInfo expectedDocument = GeneratedDocumentInfo.builder()
            .documentType(TEST_DOCUMENT_TYPE)
            .fileName(TEST_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        CaseDetails dnCourtCaseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(new HashMap<>())
            .build();
        dnCourtCaseDetails.getCaseData().putAll(dnCourtDetails);

        //given
        when(documentGeneratorClient.generatePDF(matchesDocumentInputParameters(TEST_DOCUMENT_TEMPLATE_ID, dnCourtCaseDetails), eq(AUTH_TOKEN)))
            .thenReturn(expectedDocument);

        //when
        documentGenerationTask.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDocument)));

        verify(documentGeneratorClient).generatePDF(matchesDocumentInputParameters(TEST_DOCUMENT_TEMPLATE_ID, dnCourtCaseDetails), eq(AUTH_TOKEN));
    }

}