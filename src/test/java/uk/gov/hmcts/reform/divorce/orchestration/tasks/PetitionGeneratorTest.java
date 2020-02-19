package uk.gov.hmcts.reform.divorce.orchestration.tasks;

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

import static java.util.Collections.singletonMap;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;


@RunWith(MockitoJUnitRunner.class)
public class PetitionGeneratorTest {
    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @InjectMocks
    private PetitionGenerator petitionGenerator;
    private static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        final GeneratedDocumentInfo expectedPetition = GeneratedDocumentInfo.builder()
            .documentType("foo")
            .fileName("bar")
            .build();

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                    .template(MINI_PETITION_TEMPLATE_NAME)
                    .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                    .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN)).thenReturn(expectedPetition);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DIVORCE_MINI_PETITION)).thenReturn(MINI_PETITION_TEMPLATE_NAME);
        //when
        petitionGenerator.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection =
            context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, is(newLinkedHashSet(expectedPetition)));

        verify(documentGeneratorClient).generatePDF(generateDocumentRequest, AUTH_TOKEN);
    }
}
