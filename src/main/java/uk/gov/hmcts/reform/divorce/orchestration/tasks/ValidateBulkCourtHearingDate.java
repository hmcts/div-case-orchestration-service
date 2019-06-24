package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;

@Component
public class ValidateBulkCourtHearingDate implements Task<Map<String, Object>> {

    @Autowired
    CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> bulkCaseData) throws TaskException {
        if (ccdUtil.isCcdDateTimeInThePast(String.valueOf(bulkCaseData.get(COURT_HEARING_DATE_CCD_FIELD)))) {
            throw new TaskException("Court hearing date is in the past");
        }

        return bulkCaseData;
    }
}