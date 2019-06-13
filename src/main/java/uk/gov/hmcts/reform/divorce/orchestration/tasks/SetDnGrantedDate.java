package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

@Component
public class SetDnGrantedDate implements Task<Map<String,Object>> {

    @Autowired
    CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        // Decree Nisi Granted Date is the same date as the Court Hearing Date at the time of Pronouncement
        LocalDateTime hearingDateTime = LocalDateTime.parse((String) caseData.get(COURT_HEARING_DATE_CCD_FIELD));
        caseData.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, DateUtils.formatDateFromDateTime(hearingDateTime));

        // Decree Absolute Eligible Date is 6 weeks and 1 day from the Pronouncement date
        caseData.put(DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD, ccdUtil.parseDecreeAbsoluteEligibleDate(hearingDateTime.toLocalDate()));

        return caseData;
    }
}
