package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.mapDivorceDateTimeToCCDDateTime;

@Component
public class SetDaRequestedDetailsTask implements Task<Map<String,Object>> {

    @Autowired Clock clock;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        LocalDateTime daRequestedDateTime = LocalDateTime.now(clock);
        String formattedRequestedDateTime = mapDivorceDateTimeToCCDDateTime(daRequestedDateTime);

        caseData.put(DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD, formattedRequestedDateTime);

        return caseData;
    }

}
