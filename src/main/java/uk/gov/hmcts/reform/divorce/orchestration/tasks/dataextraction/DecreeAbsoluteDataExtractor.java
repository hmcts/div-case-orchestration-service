package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation.CaseDetailsMapper;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatFromCCDFormatToHumanReadableFormat;

@Component
public class DecreeAbsoluteDataExtractor implements CaseDetailsMapper {

    private static final String COMMA = ",";
    private static final String WHO_APPLIED_FOR_DA = "petitioner";

    @Override
    public String mapCaseData(CaseDetails caseDetails) throws TaskException {
        StringBuilder csvLine = new StringBuilder();

        Map<String, Object> caseData = caseDetails.getCaseData();

        csvLine.append(System.lineSeparator());
        csvLine.append(getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));
        csvLine.append(COMMA);

        String decreeNisiGrantedDate = getMandatoryPropertyValueAsString(caseData, DECREE_NISI_GRANTED_DATE_CCD_FIELD);
        String decreeAbsoluteApplicationDate = Optional.ofNullable(caseData.get(DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD))
            .map(String.class::cast)
            .map(CcdUtil::mapCCDDateTimeToLocalDateTime)
            .map(LocalDateTime::toLocalDate)
            .map(LocalDate::toString)
            .orElse(decreeNisiGrantedDate);

        csvLine.append(formatFromCCDFormatToHumanReadableFormat(decreeAbsoluteApplicationDate));
        csvLine.append(COMMA);

        csvLine.append(formatFromCCDFormatToHumanReadableFormat(decreeNisiGrantedDate));
        csvLine.append(COMMA);

        csvLine.append(WHO_APPLIED_FOR_DA);

        return csvLine.toString();
    }

}