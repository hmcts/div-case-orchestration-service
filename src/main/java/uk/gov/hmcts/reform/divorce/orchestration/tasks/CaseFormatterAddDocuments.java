package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

@RequiredArgsConstructor
@Component
public class CaseFormatterAddDocuments implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        final Set<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        if (isNotEmpty(documentCollection)) {
            List<GeneratedDocumentInfo> generatedDocuments = documentCollection.stream()
                .map(generatedDocumentInfo ->
                    GeneratedDocumentInfo.builder()
                        .fileName(generatedDocumentInfo.getFileName())
                        .documentType(generatedDocumentInfo.getDocumentType())
                        .url(generatedDocumentInfo.getUrl())
                        .build()
                ).collect(Collectors.toList());
            return caseFormatterService.addDocuments(caseData, generatedDocuments);
        }
        return caseData;
    }
}
