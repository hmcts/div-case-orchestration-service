package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.DateFieldSetupTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public abstract class DueDateSetterTask extends DateFieldSetupTask {

    protected abstract Integer getDueDateOffsetInDays();

    @Override
    protected String getFieldName() {
        return CcdFields.DUE_DATE;
    }

    @Override
    protected String getFormattedDate(Map<String, Object> caseData) {
        return DateUtils.formatDateFromLocalDate(LocalDate.now().plus(getDueDateOffsetInDays(), ChronoUnit.DAYS));
    }
}
