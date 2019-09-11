package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.time.LocalDate;

import static java.lang.String.format;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataExtractionRequestListener implements ApplicationListener<DataExtractionRequest> {

    @Autowired
    private final DataExtractionService dataExtractionService;

    @Autowired
    private final AuthUtil authUtil;

    @Override
    public void onApplicationEvent(DataExtractionRequest event) {
        DataExtractionRequest.Status status = event.getStatus();
        LocalDate dateToExtract = event.getDate();
        log.info("Listened to {} for status {} and date {}" + DataExtractionRequest.class.getName(), status, dateToExtract);

        if (status.equals(DataExtractionRequest.Status.DA)) {
            try {
                dataExtractionService.extractCasesToFamilyMan(status, dateToExtract, authUtil.getCaseworkerToken());
            } catch (CaseOrchestrationServiceException exception) {
                String errorMessage = format("Error extracting data to Family man for status %s and date %s", status, dateToExtract.toString());
                log.error(errorMessage, exception);
                throw new RuntimeException(errorMessage, exception);
            }
        } else {
            log.warn("Ignoring data extraction request for status {}. This data extraction status is not yet implemented.", status);
        }
    }

}