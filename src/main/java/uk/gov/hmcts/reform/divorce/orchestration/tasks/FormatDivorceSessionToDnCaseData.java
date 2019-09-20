package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.DnCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class FormatDivorceSessionToDnCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        DivorceSession divorceSession = objectMapper.convertValue(sessionData, DivorceSession.class);
        DnCaseData dnCaseData = caseFormatterService.getDnCaseData(divorceSession);
        return objectMapper.convertValue(dnCaseData, Map.class);
    }
}
