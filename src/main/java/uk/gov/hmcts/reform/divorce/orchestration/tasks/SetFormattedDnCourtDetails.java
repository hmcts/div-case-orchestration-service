package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COURT_DETAILS;

@Slf4j
@Component
public class SetFormattedDnCourtDetails implements Task<Map<String, Object>> {

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        if (caseData.containsKey(COURT_NAME)) {
            try {
                DnCourt dnCourt = taskCommons.getDnCourt(String.valueOf(caseData.get(COURT_NAME)));

                context.setTransientObject(DN_COURT_DETAILS, ImmutableMap.of(
                        COURT_NAME, dnCourt.getName(),
                        COURT_CONTACT_JSON_KEY, dnCourt.getFormattedContactDetails()
                ));
            } catch (CourtDetailsNotFound exception) {
                log.warn("Decree Nisi court details not found. Will not be set in context.");
            }
        }

        return caseData;
    }
}
