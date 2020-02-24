package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_FOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_JSON_KEY;

@Component
public class CoRespondentLetterGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;
    private final DocumentTemplateService documentTemplateService;

    @Autowired
    public CoRespondentLetterGenerator(DocumentGeneratorClient documentGeneratorClient, DocumentTemplateService documentTemplateService) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.documentTemplateService = documentTemplateService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final NumberFormat poundsOnlyFormat = new DecimalFormat("#");
        final String petitionIssueFee = poundsOnlyFormat.format(((FeeResponse) context.getTransientObject(PETITION_ISSUE_FEE_JSON_KEY)).getAmount());
        final String templateId = getTemplateId(documentTemplateService, DocumentType.CO_RESPONDENT_INVITATION,
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        GeneratedDocumentInfo coRespondentInvitation =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(templateId)
                    .values(ImmutableMap.of(
                        DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails,
                        ACCESS_CODE, context.getTransientObject(CO_RESPONDENT_PIN),
                        PETITION_ISSUE_FEE_FOR_LETTER, petitionIssueFee)
                    )
                    .build(),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );

        coRespondentInvitation.setDocumentType(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION);
        coRespondentInvitation.setFileName(String.format(CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT,
                caseDetails.getCaseId()));

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION,
            new LinkedHashSet<>());
        documentCollection.add(coRespondentInvitation);

        return caseData;
    }
}
