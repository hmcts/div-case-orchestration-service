package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class BailiffSuccessServiceDueDateSetterTask extends DueDateSetterTask {

    private final Integer dueDateOffset;

    public BailiffSuccessServiceDueDateSetterTask(@Value("${bailiff.successful.dueDate}") Integer dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    protected Integer getDueDateOffsetInDays() {
        return this.dueDateOffset;
    }

    @Override
    protected String getFormattedDate() {
        LocalDate date = LocalDate.parse(CcdFields.CERTIFICATE_OF_SERVICE_DATE,
                DateTimeFormatter.ofPattern(DateUtils.Formats.CCD_DATE));
        return DateUtils.formatDateFromLocalDate(date.plus(getDueDateOffsetInDays(), ChronoUnit.DAYS));
    }
}
