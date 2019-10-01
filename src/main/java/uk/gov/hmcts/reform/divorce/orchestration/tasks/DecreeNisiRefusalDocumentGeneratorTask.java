package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.LinkedHashSet;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;

@Component
@AllArgsConstructor
public class DecreeNisiRefusalDocumentGeneratorTask implements Task<Map<String, Object>> {

    private final DocumentGeneratorClient documentGeneratorClient;

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION,
            new LinkedHashSet<>());

        if (REFUSAL_DECISION_MORE_INFO_VALUE.equalsIgnoreCase((String) caseData.get(REFUSAL_DECISION_CCD_FIELD))) {
            GeneratedDocumentInfo generatedDocumentInfo = generatePdfDocument(
                DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE,
                DECREE_NISI_REFUSAL_DOCUMENT_NAME,
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                caseDetails
            );

            documentCollection.add(generatedDocumentInfo);
        }

        return caseData;
    }

    private GeneratedDocumentInfo generatePdfDocument(String templateId, String documentType, String documentName,
                                                      String authToken, CaseDetails caseDetails) {
        final GeneratedDocumentInfo generatedDocumentInfo =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(templateId)
                    .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                    .build(),
                authToken
            );

        generatedDocumentInfo.setDocumentType(documentType);
        generatedDocumentInfo.setFileName(format(DOCUMENT_FILENAME_FMT, documentName, caseDetails.getCaseId()));

        return generatedDocumentInfo;
    }
}
