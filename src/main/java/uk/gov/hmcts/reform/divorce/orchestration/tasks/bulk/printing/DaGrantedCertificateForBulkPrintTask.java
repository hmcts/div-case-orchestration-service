package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DaGrantedCertificateForBulkPrintTask implements Task<Map<String, Object>> {

    public static final String DA_GRANTED_LETTER_TEMPLATE_ID = "FL-FRM-APP-ENG-00009.docx";
    private final PdfDocumentGenerationService pdfDocumentGenerationService;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContextKeys {
        public static final String GENERATED_DOCUMENTS = OrchestrationConstants.DOCUMENTS_GENERATED;
    }

    public DaGrantedCertificateForBulkPrintTask(PdfDocumentGenerationService pdfDocumentGenerationService) {
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        appendAnotherDocumentToBulkPrint(context, getExistingD8DocumentForDaGrantedFromCaseData(caseData));
        return caseData;
    }

    private GeneratedDocumentInfo getExistingD8DocumentForDaGrantedFromCaseData(Map<String, Object> caseData) {
        //
        return GeneratedDocumentInfo.builder()
            .documentType("daGranted")
            .url("document_url")
            .fileName("document_filename")
            .build();
    }

    public void appendAnotherDocumentToBulkPrint(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) {
        List<GeneratedDocumentInfo> documentsToBulkPrint = context.computeTransientObjectIfAbsent(
            ContextKeys.GENERATED_DOCUMENTS, new ArrayList<>()
        );

        documentsToBulkPrint.add(generatedDocumentInfo);
    }

}

/**
 * SAMPLE STRUCTURE
 *
 * {
 *   "D8DocumentsGenerated": [
 *     {
 *       "id": "7b22bf15-d055-41f8-9d77-224fed546841",
 *       "value": {
 *         "DocumentLink": {
 *           "document_url": "https://localhost:8080/documents/1234",
 *           "document_filename": "d8petition1513951627081724.pdf",
 *           "document_binary_url": "https://localhost:8080/documents/1234/binary"
 *         },
 *         "DocumentType": "petition",
 *         "DocumentComment": null,
 *         "DocumentFileName": "d8petition1513951627081724",
 *         "DocumentDateAdded": null,
 *         "DocumentEmailContent": null
 *       }
 *     },
 *     {
 *       "id": "7b22bf15-d055-41f8-9d77-224fed546841",
 *       "value": {
 *         "DocumentLink": {
 *           "document_url": "https://localhost:8080/documents/1234",
 *           "document_filename": "d8petition1513951627081724.pdf",
 *           "document_binary_url": "https://localhost:8080/documents/1234/binary"
 *         },
 *         "DocumentType": "d79",
 *         "DocumentComment": null,
 *         "DocumentFileName": "refusalOrderClarification1513951627081724",
 *         "DocumentDateAdded": null,
 *         "DocumentEmailContent": null
 *       }
 *     }
 *   ]
 * }
 */
