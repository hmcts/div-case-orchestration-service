package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.getCertificateOfServiceDateUnformatted;

@Component
public class BailiffUnsuccessServiceDueDateSetterTask extends DueDateSetterTask {

    private final Integer dueDateOffset;

    public BailiffUnsuccessServiceDueDateSetterTask(@Value("${bailiff.unsuccessful.dueDate}") Integer dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    protected Integer getDueDateOffsetInDays() {
        return this.dueDateOffset;
    }

    @Override
    protected String getFormattedDate(Map<String, Object> caseData) {
        // please see DatesDataExtractor, replace it by new method added there
        // add unit test for `getCertificateOfServiceDateUnformatted`
        String date = getCertificateOfServiceDateUnformatted(caseData);

        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DateUtils.Formats.CCD_DATE));

        return DateUtils.formatDateFromLocalDate(localDate.plus(getDueDateOffsetInDays(), ChronoUnit.DAYS));
    }
}
