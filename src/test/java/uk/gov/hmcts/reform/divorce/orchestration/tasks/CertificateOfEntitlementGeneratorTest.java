package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CERTIFICATE_OF_ENTITLEMENT;


@RunWith(MockitoJUnitRunner.class)
public class CertificateOfEntitlementGeneratorTest {
    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private CertificateOfEntitlementGenerator certificateOfEntitlementGenerator;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() throws TaskException {

        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(payload)
                .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GenerateDocumentRequest generateDocumentRequest =
                GenerateDocumentRequest.builder()
                        .template(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_NAME)
                        .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                        .build();

        final GeneratedDocumentInfo expectedRespondentAnswers =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_CERTIFICATE_OF_ENTITLEMENT)
                        .fileName(String.format(CERTIFICATE_OF_ENTITLEMENT_FILENAME_FORMAT, TEST_CASE_ID))
                        .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN))
                .thenReturn(expectedRespondentAnswers);

        //when
        certificateOfEntitlementGenerator.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedRespondentAnswers)));

        verify(documentGeneratorClient).generatePDF(generateDocumentRequest, AUTH_TOKEN);
    }

    @Test(expected = TaskException.class)
    public void throwsTaskExceptionWhenDocumentGenerationFails() throws TaskException {

        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(payload)
                .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GenerateDocumentRequest generateDocumentRequest =
                GenerateDocumentRequest.builder()
                        .template(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_NAME)
                        .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                        .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN))
                .thenThrow(FeignException.class);

        //when
        certificateOfEntitlementGenerator.execute(context, payload);
    }
}