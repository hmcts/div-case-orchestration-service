package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.LinkedHashSet;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_ANSWERS_TEMPLATE_NAME;

@Component
public class RespondentAnswersGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public RespondentAnswersGenerator(DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        CaseDetails caseDataForDoc = CaseDetails.builder().caseData(payload).build();
        GeneratedDocumentInfo respondentAnswers =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(RESPONDENT_ANSWERS_TEMPLATE_NAME)
                    .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDataForDoc))
                    .build(),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );

        respondentAnswers.setDocumentType(DOCUMENT_TYPE_RESPONDENT_INVITATION);
        respondentAnswers.setFileName(RESPONDENT_ANSWERS_TEMPLATE_NAME);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection =
            context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new LinkedHashSet<>());
        documentCollection.add(respondentAnswers);

        return payload;
    }
}
