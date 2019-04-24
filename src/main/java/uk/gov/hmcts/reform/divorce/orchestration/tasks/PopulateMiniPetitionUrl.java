package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
public class PopulateMiniPetitionUrl implements Task<Map<String, Object>>  {

    private static final String D8DOCUMENTS_GENERATED = "D8DocumentsGenerated";
    private static final String VALUE = "value";
    private static final String DOCUMENT_TYPE = "DocumentType";
    private static final String PETITION_TYPE = "petition";

    private static final String DOCUMENT_LINK = "DocumentLink";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";
    private static final String PETITION_LINK = "minipetitionlink";


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        List<Map> documentList =
                ofNullable(payload.get(D8DOCUMENTS_GENERATED)).map(i -> (List<Map>) i).orElse(new ArrayList<>());

        Map<String, Object> petitionDocument = documentList
                .stream()
                .filter(map -> PETITION_TYPE.equals(((Map) map.get(VALUE)).get(DOCUMENT_TYPE)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Petition document not found"));

        Map<String, Object> documentLink = (Map<String, Object>) ((Map) petitionDocument.get(VALUE)).get(DOCUMENT_LINK);
        payload.put(PETITION_LINK, documentLink.get(DOCUMENT_BINARY_URL));

        return payload;
    }
}
