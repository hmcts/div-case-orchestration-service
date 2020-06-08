package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskContextHelper {

    public static void addToContextDocumentCollection(TaskContext context, GeneratedDocumentInfo ...documentInfoWithMetadata) {
        context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>()).addAll(newHashSet(documentInfoWithMetadata));
    }

}
