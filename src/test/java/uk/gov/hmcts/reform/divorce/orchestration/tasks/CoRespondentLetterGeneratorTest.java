package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_AOS_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_INVITATION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_FOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_JSON_KEY;


@RunWith(MockitoJUnitRunner.class)
public class CoRespondentLetterGeneratorTest {
    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private CoRespondentLetterGenerator coRespondentLetterGenerator;

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() {
        final GeneratedDocumentInfo expectedCoRespondentInvitation =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION)
                .fileName(TEST_CO_RESPONDENT_AOS_FILE_NAME)
                .build();

        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        final FeeResponse feeResponse = FeeResponse.builder()
            .amount(550.00)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(CO_RESPONDENT_PIN, TEST_PIN);
        context.setTransientObject(PETITION_ISSUE_FEE_JSON_KEY, feeResponse);

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(CO_RESPONDENT_INVITATION_TEMPLATE_NAME)
                .values(
                    ImmutableMap.of(
                        DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails,
                        ACCESS_CODE, TEST_PIN,
                        PETITION_ISSUE_FEE_FOR_LETTER, "550")
                    )
                .build();

        //given
        when(documentGeneratorClient.generatePDF(generateDocumentRequest, AUTH_TOKEN)).thenReturn(expectedCoRespondentInvitation);

        //when
        coRespondentLetterGenerator.execute(context, payload);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection =
            context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection, CoreMatchers.is(newLinkedHashSet(expectedCoRespondentInvitation)));

        verify(documentGeneratorClient).generatePDF(generateDocumentRequest, AUTH_TOKEN);
    }
}
