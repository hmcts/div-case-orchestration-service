package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import feign.Feign;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@Component
public class RespondentLetterGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public RespondentLetterGenerator(DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;

    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        CaseDetails caseDetails = (CaseDetails) context.getTransientObject(CASE_DETAILS_JSON_KEY);

        GeneratedDocumentInfo aosInvitation =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                    .values(ImmutableMap.of(
                        DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails,
                        ACCESS_CODE, context.getTransientObject(PIN))
                    )
                    .build(),
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY))
            );

        aosInvitation.setDocumentType(DOCUMENT_TYPE_INVITATION);
        aosInvitation.setFileName(String.format(INVITATION_FILE_NAME_FORMAT,
                caseDetails.getCaseId()));

        context.setTransientObject(RESPONDENT_INVITATION_TEMPLATE_NAME, aosInvitation);

        return caseData;
    }
}
