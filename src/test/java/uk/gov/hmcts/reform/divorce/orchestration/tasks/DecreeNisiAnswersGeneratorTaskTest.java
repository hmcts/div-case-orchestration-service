package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_DN_ANSWERS;


@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiAnswersGeneratorTaskTest {
    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @InjectMocks
    private DecreeNisiAnswersGeneratorTask decreeNisiAnswersGenerator;

    private static final String DN_ANSWERS_TEMPLATE_ID = "FL-DIV-GNO-ENG-00022.docx";

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() throws TaskException {

        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(DN_ANSWERS_TEMPLATE_ID)
                .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();

        final GeneratedDocumentInfo expectedDNAnswers =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_DN_ANSWERS)
                .fileName(DOCUMENT_TYPE_DN_ANSWERS)
                .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN))
            .thenReturn(expectedDNAnswers);

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID))
                .thenReturn(DN_ANSWERS_TEMPLATE_ID);

        //when
        decreeNisiAnswersGenerator.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedDNAnswers)));

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

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(DN_ANSWERS_TEMPLATE_ID)
                .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID))
                .thenReturn(DN_ANSWERS_TEMPLATE_ID);

        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN))
            .thenThrow(FeignException.class);

        decreeNisiAnswersGenerator.execute(context, payload);
    }
}
