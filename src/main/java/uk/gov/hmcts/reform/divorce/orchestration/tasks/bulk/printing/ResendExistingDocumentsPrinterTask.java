package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;

import java.util.HashMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.getCollectionMembersOrEmptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendExistingDocumentsPrinterTask implements Task<Map<String, Object>> {
    private static final String LETTER_TYPE_EXISTING_DOCUMENTS_PACK = "existing-documents-pack";
    private static final String EXCEPTION_MSG = "There are no generated documents to resend for case: %s";
    private static final List<String> DOCUMENT_TYPES_TO_SEND = asList(
        COSTS_ORDER_DOCUMENT_TYPE, DOCUMENT_TYPE_COE, DECREE_NISI_DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE,
        DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE, DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE,
        DeemedServiceOrderGenerationTask.FileMetadata.DOCUMENT_TYPE, OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE);

    private final ObjectMapper objectMapper;
    private final BulkPrinterTask bulkPrinter;
    private final DocumentContentFetcherService documentContentFetcherService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        var generatedDocumentList = extractDocumentsFromCase(payload);
        if (generatedDocumentList.isEmpty()) {
            var msg = String.format(EXCEPTION_MSG, (String) context.getTransientObject(CASE_ID_JSON_KEY));
            log.info(msg);
            throw new TaskException(msg);
        }
        context.setTransientObject(DOCUMENTS_GENERATED, prepareForPrint(generatedDocumentList));

        return bulkPrinter.printSpecifiedDocument(context, payload, LETTER_TYPE_EXISTING_DOCUMENTS_PACK,
            filterForDocumentsToSend(generatedDocumentList));
    }

    private List<CollectionMember<Document>> extractDocumentsFromCase(Map<String, Object> caseData) {
        var documentsGenerated = getCollectionMembersOrEmptyList(objectMapper, caseData, D8DOCUMENTS_GENERATED);
        var serviceApplicationDocuments = getCollectionMembersOrEmptyList(objectMapper, caseData, SERVICE_APPLICATION_DOCUMENTS);

        return Stream.concat(documentsGenerated.stream(), serviceApplicationDocuments.stream())
            .collect(Collectors.toList());
    }

    private Map<String, GeneratedDocumentInfo> prepareForPrint(List<CollectionMember<Document>> generatedDocumentList) {
        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = new HashMap<>();
        for (CollectionMember<Document> document : generatedDocumentList) {
            Document value = document.getValue();
            String documentType = value.getDocumentType();
            DocumentLink documentLink = value.getDocumentLink();

            if (documentLink != null) {
                GeneratedDocumentInfo gdi = documentContentFetcherService.fetchPrintContent(GeneratedDocumentInfo.builder()
                    .url(documentLink.getDocumentBinaryUrl())
                    .documentType(documentType)
                    .fileName(documentLink.getDocumentFilename())
                    .build());
                generatedDocumentInfoList.put(documentType, gdi);
            }
        }

        return generatedDocumentInfoList;
    }

    private List<String> filterForDocumentsToSend(List<CollectionMember<Document>> documents) {
        return documents.stream()
            .map(collectionMember -> collectionMember.getValue().getDocumentType())
            .filter(type -> DOCUMENT_TYPES_TO_SEND.contains(type)).collect(Collectors.toList());
    }
}
