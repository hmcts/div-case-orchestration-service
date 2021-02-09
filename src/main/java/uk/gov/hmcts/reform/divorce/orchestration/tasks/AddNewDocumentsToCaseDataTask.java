package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@Component
@RequiredArgsConstructor
public class AddNewDocumentsToCaseDataTask implements Task<Map<String, Object>> {

    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        final Set<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        if (isNotEmpty(documentCollection)) {
            return ccdUtil.addNewDocumentsToCaseData(caseData, new ArrayList<>(documentCollection));
        }

        return caseData;
    }

}