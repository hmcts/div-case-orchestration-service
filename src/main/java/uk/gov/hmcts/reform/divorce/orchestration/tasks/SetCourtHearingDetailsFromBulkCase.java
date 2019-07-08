package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;

@Component
public class SetCourtHearingDetailsFromBulkCase implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> bulkCaseDetails = context.getTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY);
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseDetails.get(CCD_CASE_DATA_FIELD);

        payload.put(COURT_NAME_CCD_FIELD, bulkCaseData.get(COURT_NAME_CCD_FIELD));

        Map<String, Object> dateAndTimeOfHearing = new HashMap<>();

        LocalDateTime hearingDateTime = LocalDateTime.parse((String) bulkCaseData.get(COURT_HEARING_DATE_CCD_FIELD));

        dateAndTimeOfHearing.put(DATE_OF_HEARING_CCD_FIELD, DateUtils.formatDateFromDateTime(hearingDateTime));
        dateAndTimeOfHearing.put(TIME_OF_HEARING_CCD_FIELD, DateUtils.formatTimeFromDateTime(hearingDateTime));

        CollectionMember<Map<String, Object>> dateAndTimeOfHearingItem = new CollectionMember<>();
        dateAndTimeOfHearingItem.setValue(dateAndTimeOfHearing);

        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        Map<String, Object> caseData = caseDetails.getCaseData();

        List<CollectionMember> courtHearingCollection =
                (ArrayList) caseData.getOrDefault(DATETIME_OF_HEARING_CCD_FIELD, new ArrayList<>());
        courtHearingCollection.add(dateAndTimeOfHearingItem);

        payload.put(DATETIME_OF_HEARING_CCD_FIELD, courtHearingCollection);

        return payload;
    }
}
