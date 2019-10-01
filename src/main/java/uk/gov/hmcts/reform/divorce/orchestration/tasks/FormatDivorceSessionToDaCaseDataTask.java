package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.DaCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
public class FormatDivorceSessionToDaCaseDataTask implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormatDivorceSessionToDaCaseDataTask(CaseFormatterService caseFormatterService, ObjectMapper objectMapper) {
        this.caseFormatterService = caseFormatterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        DivorceSession divorceSession = objectMapper.convertValue(sessionData, DivorceSession.class);
        DaCaseData daCaseData = caseFormatterService.getDaCaseData(divorceSession);

        return objectMapper.convertValue(daCaseData, Map.class);
    }
}
