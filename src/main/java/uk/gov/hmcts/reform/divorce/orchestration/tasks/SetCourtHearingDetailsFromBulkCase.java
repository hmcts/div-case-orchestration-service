package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        // Set CourtName field
        courtHearingDetails.put(COURT_NAME, caseData.get(COURT_NAME));

        // Set Hearing Date and Time
        Map<String, Object> dateAndTimeOfHearing = new HashMap<>();
        try {
            LocalDateTime hearingDateTime = LocalDateTime.parse((String) caseData.get(COURT_HEARING_DATE));
            dateAndTimeOfHearing.put(DATE_OF_HEARING_CCD_FIELD,
                    hearingDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dateAndTimeOfHearing.put(TIME_OF_HEARING_CCD_FIELD,
                    hearingDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch(Exception exception) {
            throw new TaskException("Unable to parse or format bulk case court hearing date");
        }

        CollectionMember<Map<String, Object>> dateAndTimeOfHearingItem = new CollectionMember<>();

        courtHearingDetails.put(DATETIME_OF_HEARING_CCD_FIELD, Collections.singletonList(dateAndTimeOfHearingItem));

        return courtHearingDetails;
    }
}
