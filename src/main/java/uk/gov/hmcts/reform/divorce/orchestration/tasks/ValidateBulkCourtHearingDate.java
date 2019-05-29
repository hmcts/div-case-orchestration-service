package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Component
public class ValidateBulkCourtHearingDate implements Task<Map<String, Object>> {

    @Autowired
    CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> bulkCaseDetails) throws TaskException {
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseDetails.get(CCD_CASE_DATA_FIELD);
        String courtHearingDateTime = (String) bulkCaseData.get(COURT_HEARING_DATE);

        if (ccdUtil.isCcdDateTimeInThePast(courtHearingDateTime)) {
            throw new TaskException("Court hearing date is in the past");
        }

        return bulkCaseDetails;
    }
}