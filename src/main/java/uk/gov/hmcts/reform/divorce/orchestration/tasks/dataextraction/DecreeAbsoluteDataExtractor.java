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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatFromCCDFormatToHumanReadableFormat;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.mapCCDDateTimeToLocalDateTime;

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

        Optional<String> decreeAbsoluteApplicationDateOptional = Optional.ofNullable(caseData.get(DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD))
            .map(String.class::cast)
            .map(CcdUtil::mapCCDDateTimeToLocalDateTime)
            .map(LocalDateTime::toLocalDate)
            .map(LocalDate::toString);

        if (decreeAbsoluteApplicationDateOptional.isPresent()) {
            csvLine.append(formatFromCCDFormatToHumanReadableFormat(decreeAbsoluteApplicationDateOptional.get()));
        } else {
            String decreeAbsoluteGrantedDate = getMandatoryPropertyValueAsString(caseData, DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD);
            String formattedDecreeAbsoluteGrantedDate = mapCCDDateTimeToLocalDateTime(decreeAbsoluteGrantedDate).toLocalDate().toString();

            csvLine.append(formatFromCCDFormatToHumanReadableFormat(formattedDecreeAbsoluteGrantedDate));
        }
        csvLine.append(COMMA);

        String decreeNisiGrantedDate = (String) caseData.get(DECREE_NISI_GRANTED_DATE_CCD_FIELD);
        if (decreeNisiGrantedDate != null) {
            csvLine.append(formatFromCCDFormatToHumanReadableFormat(decreeNisiGrantedDate));
        }
        csvLine.append(COMMA);

        csvLine.append(WHO_APPLIED_FOR_DA);

        return csvLine.toString();
    }

}