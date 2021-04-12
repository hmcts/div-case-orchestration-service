package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseDataToDivorceFormatterTask implements Task<Map<String, Object>> {
    private final CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        log.info("---* Case data before transformation: {}", caseData);

        Map<String, Object> transformedData = caseFormatterClient.transformToDivorceFormat(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            caseData
        );

        log.info("---* Case data after transformation: {}", transformedData);

        return transformedData;
    }
}
