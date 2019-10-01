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

        final Set<uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo>
                documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        if (isNotEmpty(documentCollection)) {
            List<GeneratedDocumentInfo> generatedDocuments = documentCollection.stream().map(generatedDocumentInfo -> {
                GeneratedDocumentInfo generatedDocumentModel = new GeneratedDocumentInfo();
                generatedDocumentModel.setFileName(generatedDocumentInfo.getFileName());
                generatedDocumentModel.setDocumentType(generatedDocumentInfo.getDocumentType());
                generatedDocumentModel.setUrl(generatedDocumentInfo.getUrl());
                return generatedDocumentModel;
            }).collect(Collectors.toList());
            return caseFormatterService.addDocuments(caseData, generatedDocuments);
        }
        return caseData;
    }
}
