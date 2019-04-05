package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_CASE_FORMATTED_DATA;

@Component
public class FormatDivorceSessionToAosCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public FormatDivorceSessionToAosCaseData(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        final Map<String, Object> caseFormatAosData = caseFormatterClient.transformToAosCaseFormat(sessionData);
        context.setTransientObject(AOS_CASE_FORMATTED_DATA, caseFormatAosData);
        return caseFormatAosData;
    }
}
