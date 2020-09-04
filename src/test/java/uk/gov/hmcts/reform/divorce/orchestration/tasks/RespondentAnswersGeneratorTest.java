package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
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

@RunWith(MockitoJUnitRunner.class)
public class RespondentAnswersGeneratorTest {
    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @InjectMocks
    private RespondentAnswersGenerator respondentAnswersGenerator;

    private static final String DOCUMENT_TYPE_RESPONDENT_ANSWERS = "respondentAnswers";

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() {

        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();

        final GeneratedDocumentInfo expectedRespondentAnswers =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN))
            .thenReturn(expectedRespondentAnswers);

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.RESPONDENT_ANSWERS))
                .thenReturn(DOCUMENT_TYPE_RESPONDENT_ANSWERS);

        //when
        respondentAnswersGenerator.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedRespondentAnswers)));

        verify(documentGeneratorClient).generatePDF(generateDocumentRequest, AUTH_TOKEN);
    }
}
