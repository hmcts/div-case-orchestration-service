package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;

@Component
public class SetCourtDetails implements Task<Map<String, Object>> {

    @Autowired
    private Clock clock;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        caseData.put(CREATED_DATE_JSON_KEY, LocalDate.now(clock).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT)));

        // Assign Divorce court for Solicitor, currently hardcoded to one specific court
        caseData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());

        return caseData;
    }
}