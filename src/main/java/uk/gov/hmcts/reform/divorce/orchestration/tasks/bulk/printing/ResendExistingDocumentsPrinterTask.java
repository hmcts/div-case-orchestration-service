package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;

@Slf4j
@Component
public class ResendExistingDocumentsPrinterTask implements Task<Map<String, Object>> {
    private static final String LETTER_TYPE_EXISTING_DOCUMENTS_PACK = "existing-documents-pack";
    private static final String VALUE_KEY = "value";
    private static final String EXCEPTION_MSG = "There are no generated documents to resend for case: %s";
    private static final List<String> DOCUMENT_TYPES_TO_SEND = asList(
        COSTS_ORDER_DOCUMENT_TYPE, DOCUMENT_TYPE_COE, DECREE_NISI_DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE,
        DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE, DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE,
        DeemedServiceOrderGenerationTask.FileMetadata.DOCUMENT_TYPE, OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE);

    private final BulkPrinterTask bulkPrinter;

    @Autowired
    public ResendExistingDocumentsPrinterTask(final BulkPrinterTask bulkPrinter) {
        this.bulkPrinter = bulkPrinter;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        var allGeneratedDocuments = extractDocumentsFromCase(payload);
        if (allGeneratedDocuments.isEmpty()) {
            var msg = String.format(EXCEPTION_MSG, (String) context.getTransientObject(CASE_ID_JSON_KEY));
            log.info(msg);
            throw new TaskException(msg);
        }

        return bulkPrinter.printSpecifiedDocument(context, payload, LETTER_TYPE_EXISTING_DOCUMENTS_PACK,
            filterForDocumentsToSend(allGeneratedDocuments));
    }

    private List<Map<String, Object>> extractDocumentsFromCase(Map<String, Object> caseData) {
        var documentsGenerated = (List<Map<String, Object>>) caseData.get(D8DOCUMENTS_GENERATED);
        if (documentsGenerated == null) {
            documentsGenerated = new ArrayList<>();
        }

        var serviceApplicationDocuments = (List<Map<String, Object>>) caseData.get(SERVICE_APPLICATION_DOCUMENTS);
        if (serviceApplicationDocuments == null) {
            serviceApplicationDocuments = new ArrayList<>();
        }

        return Stream.concat(documentsGenerated.stream(), serviceApplicationDocuments.stream())
            .collect(Collectors.toList());
    }

    private List<String> filterForDocumentsToSend(List<Map<String, Object>> documents) {
        return documents.stream().map(collectionMember -> {
            var document = (Map<String, Object>) collectionMember.get(VALUE_KEY);
            return (String) document.get(DOCUMENT_TYPE_JSON_KEY);
        }).filter(type -> DOCUMENT_TYPES_TO_SEND.contains(type)).collect(Collectors.toList());
    }
}
