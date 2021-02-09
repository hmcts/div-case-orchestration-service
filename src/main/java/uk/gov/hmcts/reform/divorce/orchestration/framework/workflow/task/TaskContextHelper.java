package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class TaskContextHelper {

    public static void failTask(TaskContext context, String errorKey, List<String> errorValues) {
        context.setTaskFailed(true);
        context.setTransientObject(errorKey, errorValues);
        log.info("Tasked execution failed. Key: {} with errors {}", errorKey, errorValues.toArray());
    }

    public static void addToContextDocumentCollection(TaskContext context,
                                                      GeneratedDocumentInfo documentInfoWithMetadata) {
        Set<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>());

        if (isExistingDocument(documentCollection, documentInfoWithMetadata)) {
            throw new InvalidDataForTaskException(new TaskException(documentInfoWithMetadata.getDocumentType()
                + " already exists in context's DOCUMENT_COLLECTION"));
        }

        context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>()).add(documentInfoWithMetadata);
    }

    public static void addAllToContextDocumentCollection(TaskContext context, List<GeneratedDocumentInfo> generatedDocumentInfoList) {
        generatedDocumentInfoList.forEach(documentInfo -> addToContextDocumentCollection(context, documentInfo));
    }

    private static boolean isExistingDocument(Set<GeneratedDocumentInfo> documentCollection, GeneratedDocumentInfo documentInfoWithMetadata) {
        return documentCollection.contains(documentInfoWithMetadata);
    }

}
