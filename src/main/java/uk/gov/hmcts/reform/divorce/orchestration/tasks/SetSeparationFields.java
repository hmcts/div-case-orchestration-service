package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_MENTAL_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PHYSICAL_SEP_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_DESERTION_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_REF_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_TIME_TOGETHER_PERMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_5YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SetSeparationFields implements Task<Map<String, Object>> {

    public static final Integer TWO = 2;
    public static final Integer FIVE = 5;
    public static final Integer SIX = 6;
    public static final Integer SEVEN = 7;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String separationTimeTogetherPermitted = getSeparationTimeTogetherPermitted(caseData);
        String sepReferenceDate = DateUtils.formatDateWithCustomerFacingFormat(getReferenceDate(caseData));
        String mostRecentSeperationDate = getReasonForDivorceSeparationDate(caseData);

        caseData.put(D_8_REASON_FOR_DIVORCE_SEP_DATE, mostRecentSeperationDate);
        caseData.put(D_8_SEP_REF_DATE, sepReferenceDate);
        caseData.put(D_8_SEP_TIME_TOGETHER_PERMITTED, separationTimeTogetherPermitted);

        return caseData;
    }

    private int getSepYears(Map<String, Object> caseData) throws TaskException {
        String reasonForDivorce = getMandatoryPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE);
        if (StringUtils.equalsIgnoreCase(SEPARATION_5YRS, reasonForDivorce)) {
            return FIVE;
        }
        return TWO;
    }

    private String getReasonForDivorceSeparationDate(Map<String, Object> caseData) throws TaskException {
        String reasonForDivorce = getMandatoryPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE);
        if (StringUtils.equalsIgnoreCase(DESERTION, reasonForDivorce)) {
            String reasonForDivorceDesertionDate = getMandatoryPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE_DESERTION_DAIE);
            return reasonForDivorceDesertionDate;
        }

        String reasonForDivorceDecisionDate = getMandatoryPropertyValueAsString(caseData, D_8_MENTAL_SEP_DATE);
        String reasonForDivorceLivingApartDate = getMandatoryPropertyValueAsString(caseData, D_8_PHYSICAL_SEP_DAIE);
        if (LocalDate.parse(reasonForDivorceDecisionDate).compareTo(LocalDate.parse(reasonForDivorceLivingApartDate)) > 0) {
            return reasonForDivorceDecisionDate;
        }
        return reasonForDivorceLivingApartDate;
    }

    private LocalDate getDateBeforeSepYears(Map<String, Object> caseData) throws TaskException {
        return LocalDate.now().minusYears(getSepYears(caseData));
    }

    private Long getLivingTogetherMonths(Map<String, Object> caseData) throws TaskException {
        LocalDate dateBeforeSepYears = getDateBeforeSepYears(caseData);
        LocalDate sepDate = LocalDate.parse(getReasonForDivorceSeparationDate(caseData));
        return ChronoUnit.MONTHS.between(dateBeforeSepYears, sepDate);
    }

    private Long getLivingTogetherWeeks(Map<String, Object> caseData) throws TaskException {
        LocalDate dateBeforeSepYears = getDateBeforeSepYears(caseData);
        LocalDate sepDate = LocalDate.parse(getReasonForDivorceSeparationDate(caseData));
        return ChronoUnit.WEEKS.between(dateBeforeSepYears, sepDate);
    }

    private Long getLivingTogetherDays(Map<String, Object> caseData) throws TaskException {
        LocalDate dateBeforeSepYears = getDateBeforeSepYears(caseData);
        LocalDate sepDate = LocalDate.parse(getReasonForDivorceSeparationDate(caseData));
        return ChronoUnit.DAYS.between(dateBeforeSepYears, sepDate);
    }

    private LocalDate getReferenceDate(Map<String, Object> caseData) throws TaskException {
        return getDateBeforeSepYears(caseData).minusMonths(SIX);
    }

    private String getSeparationTimeTogetherPermitted(Map<String, Object> caseData) throws TaskException {
        Long timeTogetherMonths = getLivingTogetherMonths(caseData);
        Long timeTogetherWeeks = getLivingTogetherWeeks(caseData);
        Long timeTogetherDays = getLivingTogetherDays(caseData) % SEVEN;

        StringBuilder permittedSepTime = new StringBuilder("");
        if (timeTogetherMonths >= SIX) {
            permittedSepTime.append("6 months");
            return permittedSepTime.toString();
        }
        if (timeTogetherWeeks == 1) {
            permittedSepTime.append(timeTogetherWeeks);
            permittedSepTime.append(" week");
        } else if (timeTogetherWeeks > 1) {
            permittedSepTime.append(timeTogetherWeeks);
            permittedSepTime.append(" weeks");
        }
        if (timeTogetherWeeks > 0 && timeTogetherDays > 0) {
            permittedSepTime.append(" and");
        }
        if (timeTogetherDays == 1) {
            permittedSepTime.append(timeTogetherDays);
            permittedSepTime.append(" day");
        } else if (timeTogetherDays > 1) {
            permittedSepTime.append(timeTogetherDays);
            permittedSepTime.append(" days");
        }
        return permittedSepTime.toString();
    }
}