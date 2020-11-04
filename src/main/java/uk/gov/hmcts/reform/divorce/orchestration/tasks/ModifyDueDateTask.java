package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.DateFieldSetupTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ModifyDueDateTask extends DateFieldSetupTask {

    private final Integer dueDateOffset;

    public ModifyDueDateTask(@Value("${bulk-print.dueDate}") Integer dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    protected String getFieldName() {
        return CcdFields.DUE_DATE;
    }

    @Override
    protected String getFormattedDate() {
        return DateUtils.formatDateFromLocalDate(LocalDate.now().plus(dueDateOffset, ChronoUnit.DAYS));
    }
}
