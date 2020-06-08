package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.HashSet;
import java.util.List;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskContextHelper {

    public static void addToContextDocumentCollection(TaskContext context,
                                                      GeneratedDocumentInfo documentInfoWithMetadata)  throws InvalidDataForTaskException {
        HashSet<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>());

        if (isExistingDocument(documentCollection, documentInfoWithMetadata)) {
            throw new InvalidDataForTaskException(new TaskException(documentInfoWithMetadata.getDocumentType()
                + " already exists in context DOCUMENT_COLLECTION"));
        }

        context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>()).add(documentInfoWithMetadata);
    }

    public static void addAllToContextDocumentCollection(TaskContext context, List<GeneratedDocumentInfo> generatedDocumentInfoList) {
        generatedDocumentInfoList.forEach(documentInfo -> addToContextDocumentCollection(context, documentInfo));
    }

    private static boolean isExistingDocument(HashSet<GeneratedDocumentInfo> documentCollection, GeneratedDocumentInfo documentInfoWithMetadata) {
        return documentCollection.contains(documentInfoWithMetadata);
    }

}
