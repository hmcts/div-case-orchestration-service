package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPLICATION_SUBMITTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.retrieveAndFormatCCDDateFieldIfPresent;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatFromCCDFormatToHumanReadableFormat;

@Component
@Slf4j
public class AOSDataExtractor implements CSVExtractor {

    private static final String COMMA = ",";
    private static final String FILE_NAME_PREFIX = "AOSDN";

    private final String destinationEmailAddress;

    public AOSDataExtractor(@Value("${dataExtraction.status.AOS.emailTo}") String destinationEmailAddress) {
        this.destinationEmailAddress = destinationEmailAddress;
    }

    @Override
    public Stream<String> getRelevantCaseStates() {
        return Stream.of(AWAITING_LEGAL_ADVISOR_REFERRAL);
    }

    @Override
    public String getFileNamePrefix() {
        return FILE_NAME_PREFIX;
    }

    @Override
    public String getDestinationEmailAddress() {
        return destinationEmailAddress;
    }

    @Override
    public String getHeaderLine() {
        return "CaseReferenceNumber,ReceivedAOSFromResDate,ReceivedAOSFromCoResDate,ReceivedDNApplicationDate";
    }

    @Override
    public Optional<String> mapCaseData(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        Optional<String> transformedCaseData;

        try {
            StringBuilder csvLine = new StringBuilder();

            csvLine.append(System.lineSeparator());
            csvLine.append(getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));
            csvLine.append(COMMA);

            String receivedAOSFromRespDate = retrieveAndFormatCCDDateFieldIfPresent(RECEIVED_AOS_FROM_RESP_DATE, caseData, "" );
            csvLine.append(receivedAOSFromRespDate);
            csvLine.append(COMMA);

            String receivedAOSFromCoRespDate = retrieveAndFormatCCDDateFieldIfPresent(RECEIVED_AOS_FROM_CO_RESP_DATE, caseData, "");
            csvLine.append(receivedAOSFromCoRespDate);
            csvLine.append(COMMA);

            String receivedDnApplicationDate = getMandatoryPropertyValueAsString(caseData,DN_APPLICATION_SUBMITTED_DATE);
            String receivedDnApplicationDateFormatted = formatFromCCDFormatToHumanReadableFormat(receivedDnApplicationDate);
            csvLine.append(receivedDnApplicationDateFormatted);

            transformedCaseData = Optional.of(csvLine.toString());
        } catch (TaskException exception) {
            log.error("Ignoring case {} because of missing mandatory fields.", caseDetails.getCaseId(), exception);
            transformedCaseData = Optional.empty();
        }

        return transformedCaseData;
    }

}