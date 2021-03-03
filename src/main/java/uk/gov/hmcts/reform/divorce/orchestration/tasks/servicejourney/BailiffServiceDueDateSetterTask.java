package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.getCertificateOfServiceDateUnformatted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Slf4j
public abstract class BailiffServiceDueDateSetterTask extends DueDateSetterTask {

    private final Integer dueDateOffset;

    protected BailiffServiceDueDateSetterTask(int dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        final String caseId = getCaseId(context);
        final String fieldName = getFieldName();
        final String formattedDateValue = getCertificateOfServiceFormattedDate(caseData);

        log.info("CaseID: {} setting up date {} for field {}", caseId, formattedDateValue, fieldName);

        caseData.put(fieldName, formattedDateValue);

        return caseData;
    }

    @Override
    protected Integer getDueDateOffsetInDays() {
        return dueDateOffset;
    }

    protected String getCertificateOfServiceFormattedDate(Map<String, Object> caseData) {
        String date = getCertificateOfServiceDateUnformatted(caseData);

        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DateUtils.Formats.CCD_DATE));
        return DateUtils.formatDateFromLocalDate(localDate.plus(getDueDateOffsetInDays(), ChronoUnit.DAYS));
    }
}
