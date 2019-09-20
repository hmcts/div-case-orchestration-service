package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;


@RequiredArgsConstructor
@Component
public class FormatDivorceSessionToCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        DivorceSession divorceSession = objectMapper.convertValue(payload, DivorceSession.class);
        CoreCaseData coreCaseData = caseFormatterService.transformToCCDFormat(divorceSession, null);
        return objectMapper.convertValue(coreCaseData, Map.class);
    }

}