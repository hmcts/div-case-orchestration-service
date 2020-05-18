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

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint;

@Component
@AllArgsConstructor
public class AddDaGrantedCertificateToDocumentsToPrintTask implements Task<Map<String, Object>> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String DOCUMENT_TYPE = OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
    }

    private final DocumentContentFetcherService documentContentFetcherService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        appendAnotherDocumentToBulkPrint(
            context,
            getExistingDaGrantedFromCaseData(caseData)
        );

        return caseData;
    }

    private GeneratedDocumentInfo getExistingDaGrantedFromCaseData(Map<String, Object> caseData) {
        GeneratedDocumentInfo generatedDocumentInfo = DaGrantedCertificateDataExtractor.getDaGrantedDocumentInformPartiallyPopulated(caseData)
            .documentType(FileMetadata.DOCUMENT_TYPE)
            .build();

        return getContentOfDocumentFromDocStore(generatedDocumentInfo);
    }

    private GeneratedDocumentInfo getContentOfDocumentFromDocStore(GeneratedDocumentInfo documentInfo) {
        return documentContentFetcherService.fetchPrintContent(documentInfo);
    }
}
