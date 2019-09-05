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
        try {
            dataExtractionService.extractCasesToFamilyMan(event.getStatus(), event.getDate(), authUtil.getCaseworkerToken());
        } catch (CaseOrchestrationServiceException exception) {
            String errorMessage = format("Error extracting data to Family man for %s", event.getDate().toString());
            log.error(errorMessage, exception);
            throw new RuntimeException(errorMessage, exception);
        }
    }

}