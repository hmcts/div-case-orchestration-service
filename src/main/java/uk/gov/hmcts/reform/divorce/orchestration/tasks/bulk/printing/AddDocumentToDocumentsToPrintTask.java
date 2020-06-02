package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DocumentDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint;

@Component
@AllArgsConstructor
public class AddDocumentToDocumentsToPrintTask implements Task<Map<String, Object>> {
    public static final String DOCUMENT_TYPE_TO_ADD_TO_PRINT = "documentTypeToAddToPrint";

    private final DocumentContentFetcherService documentContentFetcherService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String documentType = context.getTransientObject(DOCUMENT_TYPE_TO_ADD_TO_PRINT);
        appendAnotherDocumentToBulkPrint(
            context,
            getSpecifiedDocumentFromCaseData(caseData, documentType)
        );

        return caseData;
    }

    private GeneratedDocumentInfo getSpecifiedDocumentFromCaseData(Map<String, Object> caseData, String documentType) {
        GeneratedDocumentInfo generatedDocumentInfo = DocumentDataExtractor.getDocumentInformPartiallyPopulated(caseData, documentType)
            .documentType(documentType)
            .build();

        return getContentOfDocumentFromDocStore(generatedDocumentInfo);
    }

    private GeneratedDocumentInfo getContentOfDocumentFromDocStore(GeneratedDocumentInfo documentInfo) {
        return documentContentFetcherService.fetchPrintContent(documentInfo);
    }
}
