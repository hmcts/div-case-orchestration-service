package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@RequiredArgsConstructor
@Slf4j
public abstract class DateFieldSetupTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        final String caseId = getCaseId(context);
        final String fieldName = getFieldName();
        final String formattedDateValue = getFormattedDate();

        log.info("CaseID: {} setting up date {} for field {}", caseId, formattedDateValue, fieldName);

        caseData.put(fieldName, formattedDateValue);

        return caseData;
    }

    protected abstract String getFieldName();

    /**
     * Overwrite if you need other date than default CCD date format of today.
     */
    protected String getFormattedDate() {
        return getFormattedStringDateForTodayAsDefault();
    }

    private String getFormattedStringDateForTodayAsDefault() {
        return DateUtils.formatDateFromLocalDate(LocalDate.now());
    }
}
