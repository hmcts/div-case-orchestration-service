package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;

@Slf4j
@Component
/**
 * Class that will request documents to be bulk printed.
 * The order of the documents is important for this class.
 * The first document type in this list is the first piece of paper in the envelope. It should contain the address label to be displayed.
 */
public class BulkPrinterTask implements Task<Map<String, Object>> {

    public static final String BULK_PRINT_LETTER_TYPE = "bulkPrintLetterType";
    public static final String DOCUMENT_TYPES_TO_PRINT = "documentTypesToPrint";

    private final BulkPrintService bulkPrintService;

    @Autowired
    public BulkPrinterTask(final BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> payload) {

        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String bulkPrintLetterType = context.getTransientObject(BULK_PRINT_LETTER_TYPE);

        final Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = context.getTransientObject(DOCUMENTS_GENERATED);
        final List<String> documentTypesToPrint = context.getTransientObject(DOCUMENT_TYPES_TO_PRINT);
        final List<GeneratedDocumentInfo> documentsToPrint = documentTypesToPrint.stream()
            .map(generatedDocumentInfoList::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        //Make sure every requested document type was found
        if (documentTypesToPrint.size() == documentsToPrint.size()) {
            try {
                bulkPrintService.send(caseDetails.getCaseId(), bulkPrintLetterType, documentsToPrint);
            } catch (final Exception e) {
                context.setTaskFailed(true);
                log.error("Respondent pack bulk print failed for case {}", caseDetails.getCaseId(), e);
                context.setTransientObject(BULK_PRINT_ERROR_KEY, "Bulk print failed for " + bulkPrintLetterType);
            }
        } else {
            log.warn(
                "Bulk print for case {} is misconfigured. Documents types expected: {}, actual documents: {}",
                caseDetails.getCaseId(),
                documentTypesToPrint,
                documentsToPrint
            );
            context.setTaskFailed(true);
            context.setTransientObject(BULK_PRINT_ERROR_KEY, "Bulk print didn't kicked off for " + bulkPrintLetterType);
        }

        return payload;
    }

    public Map<String, Object> printSpecifiedDocument(TaskContext context,
                                                      Map<String, Object> payload,
                                                      String letterType,
                                                      List<String> orderedDocumentTypesToPrint) {
        final Object originalBulkPrintLetterType = context.getTransientObject(BULK_PRINT_LETTER_TYPE);
        final Object originalDocumentTypesToPrint = context.getTransientObject(DOCUMENT_TYPES_TO_PRINT);

        context.setTransientObject(BULK_PRINT_LETTER_TYPE, letterType);
        context.setTransientObject(DOCUMENT_TYPES_TO_PRINT, orderedDocumentTypesToPrint);

        Map<String, Object> returnedPayload = execute(context, payload);

        context.setTransientObject(BULK_PRINT_LETTER_TYPE, originalBulkPrintLetterType);
        context.setTransientObject(DOCUMENT_TYPES_TO_PRINT, originalDocumentTypesToPrint);

        return returnedPayload;
    }

}