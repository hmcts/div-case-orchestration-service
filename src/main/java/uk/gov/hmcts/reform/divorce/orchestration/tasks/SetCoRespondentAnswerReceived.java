package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SetCoRespondentAnswerReceived implements Task<Map<String, Object>> {

    @Autowired
    private Clock clock;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        payload.put(CO_RESPONDENT_ANSWER_RECEIVED, YES_VALUE);
        payload.put(CO_RESPONDENT_ANSWER_RECEIVED_DATE, LocalDate.now(clock).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT)));
        return payload;
    }
}
