package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@Component
public class CaseDataDraftToDivorceFormatterTask implements Task<Map<String, Object>> {
    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDataDraftToDivorceFormatterTask(CaseFormatterService caseFormatterService, ObjectMapper objectMapper) {
        this.caseFormatterService = caseFormatterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        if (isDraft(caseData)) {
            return transformToDivorceSession(caseData);
        }

        return caseData;
    }

    private boolean isDraft(Map<String, Object> caseData) {
        Object isDraftObject = caseData.get(IS_DRAFT_KEY);
        return isDraftObject == null || !(Boolean) isDraftObject;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> transformToDivorceSession(Map<String, Object> data) {
        try {
            CoreCaseData coreCaseData = objectMapper.convertValue(data, CoreCaseData.class);

            Map<String, Object> formattedData = objectMapper.convertValue(
                caseFormatterService.transformToDivorceSession(coreCaseData),
                Map.class
            );
            formattedData.remove("expires");

            return formattedData;
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException(exception);
        }
    }
}