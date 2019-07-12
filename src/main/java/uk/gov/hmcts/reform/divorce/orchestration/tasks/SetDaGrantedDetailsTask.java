package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.mapDivorceDateTimeToCCDDateTime;

@Component
public class SetDaGrantedDetailsTask implements Task<Map<String,Object>> {

    @Autowired CcdUtil ccdUtil;

    @Autowired Clock clock;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        if (!isJudgeAssigned(caseData)) {
            throw new TaskException("Judge who pronounced field must be set.");
        }

        LocalDateTime grantedDateTime = LocalDateTime.now(clock);
        String formattedGrantedDateTime = mapDivorceDateTimeToCCDDateTime(grantedDateTime);

        caseData.put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, formattedGrantedDateTime);

        return caseData;
    }

    private boolean isJudgeAssigned(Map<String, Object> caseData) {
        return nonNull(caseData.get(PRONOUNCEMENT_JUDGE_CCD_FIELD));
    }
}
