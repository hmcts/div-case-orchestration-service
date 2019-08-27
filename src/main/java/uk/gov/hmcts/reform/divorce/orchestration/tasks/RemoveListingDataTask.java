package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@Component
public class RemoveListingDataTask implements Task<Map<String, Object>> {

    @Autowired
    private ObjectMapper mapper;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        Map<String, Object> response = new HashMap<>(payload);
        response.remove(BULK_LISTING_CASE_ID_FIELD);
        response.remove(COURT_NAME_CCD_FIELD);
        response.remove(PRONOUNCEMENT_JUDGE_CCD_FIELD);
        response.remove(DATETIME_OF_HEARING_CCD_FIELD);

        return response;
    }
}
