package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
@RequiredArgsConstructor
public class PopulateDocLinkTask implements Task<Map<String, Object>> {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        List<CollectionMember<Document>> documentList =
            ofNullable(payload.get(D8DOCUMENTS_GENERATED))
                .map(i -> objectMapper.convertValue(i, new TypeReference<List<CollectionMember<Document>>>() {
                }))
                .orElse(new ArrayList<>());

        String ccdDocumentType = context.getTransientObject(DOCUMENT_TYPE);
        String docLinkFieldName = context.getTransientObject(DOCUMENT_DRAFT_LINK_FIELD);
        if (StringUtils.isNotBlank(ccdDocumentType)) {
            CollectionMember<Document> petitionDocument = documentList
                .stream()
                .filter(document -> ccdDocumentType.equals(document.getValue().getDocumentType()))
                .findFirst()
                .orElseThrow(() -> new TaskException(ccdDocumentType + " document not found"));

            payload.put(docLinkFieldName, petitionDocument.getValue().getDocumentLink());
        }

        return payload;
    }
}
