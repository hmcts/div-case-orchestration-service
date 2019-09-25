package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatFromCCDFormatToHumanReadableFormat;

@Component
@Slf4j
public class DecreeNisiDataExtractor implements CSVExtractor {

    private static final String FILE_NAME_PREFIX = "DN";

    private final String destinationEmailAddress;

    public DecreeNisiDataExtractor(
        @Value("${dataExtraction.status.DN.emailTo}") String destinationEmailAddress) {
        this.destinationEmailAddress = destinationEmailAddress;
    }

    @Override
    public String getHeaderLine() {
        return "CaseReferenceNumber,CofEGrantedDate,HearingDate,HearingTime,PlaceOfHearing,OrderForCosts,"
                + "PartyToPayCosts,CostsToBeAssessed,OrderForAncilliaryRelief,OrderOrCauseList,JudgesName";
    }

    @Override
    public String getDestinationEmailAddress() {
        return destinationEmailAddress;
    }

    @Override
    public String getFileNamePrefix() {
        return FILE_NAME_PREFIX;
    }

    @Override
    public Stream<String> getRelevantCaseStates() {
        return Stream.of(DN_REFUSED, DN_PRONOUNCED);
    }

    @Override
    public Optional<String> mapCaseData(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        Optional<String> transformedCaseData;

        try {
            StringBuilder csvLine = new StringBuilder();

            csvLine.append(getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));
            csvLine.append(COMMA);

            String formattedDnDecisionDate = formatFromCCDFormatToHumanReadableFormat(
                    getMandatoryPropertyValueAsString(caseData, DN_DECISION_DATE_FIELD)
            );

            csvLine.append(formattedDnDecisionDate);
            csvLine.append(COMMA);

            LocalDateTime hearingDateTime = LocalDateTime.parse(getMandatoryPropertyValueAsString(
                caseData,
                DATETIME_OF_HEARING_CCD_FIELD
            ));

            String hearingDate = DateUtils.formatDateFromDateTime(hearingDateTime);
            String hearingTime = DateUtils.formatTimeFromDateTime(hearingDateTime);

            csvLine.append(formatFromCCDFormatToHumanReadableFormat(hearingDate));
            csvLine.append(COMMA);
            csvLine.append(hearingTime);
            csvLine.append(COMMA);
            csvLine.append(getMandatoryPropertyValueAsString(caseData, COURT_NAME_CCD_FIELD));
            csvLine.append(COMMA);
            csvLine.append(getMandatoryPropertyValueAsString(caseData, DIVORCE_COSTS_CLAIM_CCD_FIELD));
            csvLine.append(COMMA);
            csvLine.append(getOptionalPropertyValueAsString(caseData, WHO_PAYS_COSTS_CCD_FIELD, ""));
            csvLine.append(COMMA);
            csvLine.append(getMandatoryPropertyValueAsString(caseData, COSTS_CLAIM_GRANTED));
            csvLine.append(COMMA);
            csvLine.append("No"); // OrderForAncilliaryRelief will always "no"
            csvLine.append(COMMA);
            csvLine.append("Order"); //OrderOrCauseList will always be "order"
            csvLine.append(COMMA);
            csvLine.append(getMandatoryPropertyValueAsString(caseData, PRONOUNCEMENT_JUDGE_CCD_FIELD));


            transformedCaseData = Optional.of(csvLine.toString());
        } catch (TaskException exception) {
            log.error(format("Ignoring case %s because of missing mandatory fields.", caseDetails.getCaseId()), exception);
            transformedCaseData = Optional.empty();
        }

        return transformedCaseData;
    }
}
