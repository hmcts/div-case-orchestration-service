package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsLocalDateFromCCD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatDateForCCD;

@Component
public class CalculateDecreeAbsoluteDates implements Task<Map<String, Object>> {

    private static final Period TIME_UNTIL_APPLICANT_CAN_APPLY_FOR_DA = Period.ofWeeks(6).plusDays(1);

    private static final TemporalAmount TIME_UNTIL_RESPONDENT_CAN_APPLY_FOR_DA = TIME_UNTIL_APPLICANT_CAN_APPLY_FOR_DA.plusMonths(3);
    private static final TemporalAmount TIME_UNTIL_CASE_IS_NO_LONGER_ELIGIBLE_FOR_DA = Period.ofYears(1);

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        LocalDate decreeNisiGrantedDate = getMandatoryPropertyValueAsLocalDateFromCCD(payload, DECREE_NISI_GRANTED_DATE_CCD_FIELD);

        Map<String, Object> payloadToModify = new HashMap<>(payload);
        payloadToModify.put(DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD,
            formatDateForCCD(decreeNisiGrantedDate.plus(TIME_UNTIL_RESPONDENT_CAN_APPLY_FOR_DA)));
        payloadToModify.put(DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD,
            formatDateForCCD(decreeNisiGrantedDate.plus(TIME_UNTIL_CASE_IS_NO_LONGER_ELIGIBLE_FOR_DA)));

        return payloadToModify;
    }

}