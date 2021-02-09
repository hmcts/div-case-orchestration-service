package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.BulkPrintConfig;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiBulkPrinterTask implements Task<Map<String, Object>> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContextFields {
        public static final String MULTI_BULK_PRINT_CONFIGS = "multiBulkPrintConfigs";
    }

    private final BulkPrinterTask bulkPrinterTask;

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> payload) throws TaskException {
        List<BulkPrintConfig> configs = getConfigsForMultiBulkPrint(context);
        final String caseId = getCaseId(context);

        log.info("CaseID: {} Multi Bulk Printer config is {}.", caseId, toLogMessage(configs));

        for (BulkPrintConfig bulkPrintConfig : configs) {
            if (!bulkPrintConfig.getDocumentTypesToPrint().isEmpty()) {
                log.info("CaseID: {} calling bulk printer task for {}.", caseId, bulkPrintConfig.getBulkPrintLetterType());
                triggerBulkPrint(context, payload, bulkPrintConfig);
            } else {
                log.warn("CaseID: {} no documents for {}.", caseId, bulkPrintConfig.getBulkPrintLetterType());
            }
        }

        log.info("CaseID: {} triggering valid bulk prints finished.", caseId);

        return payload;
    }

    private void triggerBulkPrint(TaskContext context, Map<String, Object> payload, BulkPrintConfig bulkPrintConfig)
        throws TaskException {
        final String letterType = bulkPrintConfig.getBulkPrintLetterType();
        final String caseId = getCaseId(context);

        log.info(
            "CaseID: {} Bulk Printer will be called with letter-type = {} and documents = {}.",
            caseId, letterType, bulkPrintConfig.getDocumentTypesToPrint()
        );

        bulkPrinterTask.printSpecifiedDocument(context, payload, letterType, bulkPrintConfig.getDocumentTypesToPrint());

        log.info("CaseID: {} Bulk Printer called for letter type = {}.", caseId, letterType);
    }

    private List<BulkPrintConfig> getConfigsForMultiBulkPrint(TaskContext context) {
        return context.computeTransientObjectIfAbsent(
            ContextFields.MULTI_BULK_PRINT_CONFIGS, new ArrayList<>()
        );
    }

    private String toLogMessage(List<BulkPrintConfig> configs) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("prepared for %s sets of documents. (", configs.size()));

        for (BulkPrintConfig bulkPrintConfig : configs) {
            stringBuilder.append(
                String.format(
                    "[documentType = %s, bulkPrintLetterType = %s], ",
                    bulkPrintConfig.getDocumentTypesToPrint(),
                    bulkPrintConfig.getBulkPrintLetterType()
                )
            );
        }

        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
