package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;

@Component
public class SetCourtHearingDetailsFromBulkCase implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Map<String, Object> courtHearingDetails = new HashMap<>();

        courtHearingDetails.put(COURT_NAME, caseData.get(COURT_NAME));

        Map<String, Object> dateAndTimeOfHearing = new HashMap<>();

        LocalDateTime hearingDateTime = LocalDateTime.parse((String) caseData.get(COURT_HEARING_DATE));

        dateAndTimeOfHearing.put(DATE_OF_HEARING_CCD_FIELD, DateUtils.formatLetterDateFromDateTime(hearingDateTime));
        dateAndTimeOfHearing.put(TIME_OF_HEARING_CCD_FIELD, DateUtils.formatLetterTimeFromDateTime(hearingDateTime));

        CollectionMember<Map<String, Object>> dateAndTimeOfHearingItem = new CollectionMember<>();
        dateAndTimeOfHearingItem.setValue(dateAndTimeOfHearing);

        courtHearingDetails.put(DATETIME_OF_HEARING_CCD_FIELD, Collections.singletonList(dateAndTimeOfHearingItem));

        return courtHearingDetails;
    }
}
