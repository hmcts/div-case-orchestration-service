package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;

@Component
public class PetitionGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;
    private final DocumentTemplateService documentTemplateService;

    @Autowired
    public PetitionGenerator(DocumentGeneratorClient documentGeneratorClient, DocumentTemplateService documentTemplateService) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.documentTemplateService = documentTemplateService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String templateId = getTemplateId(documentTemplateService, DocumentType.DIVORCE_MINI_PETITION,
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        GeneratedDocumentInfo miniPetition =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(templateId)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                                        caseDetails))
                                .build(),
                    context.getTransientObject(AUTH_TOKEN_JSON_KEY)
                );

        miniPetition.setDocumentType(DOCUMENT_TYPE_PETITION);
        miniPetition.setFileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION,
            new LinkedHashSet<>());
        documentCollection.add(miniPetition);

        return caseData;
    }
}
