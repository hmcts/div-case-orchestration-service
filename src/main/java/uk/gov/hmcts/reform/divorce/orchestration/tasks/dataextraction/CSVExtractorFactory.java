package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;

@Component
public class CSVExtractorFactory {

    public CSVExtractor getCSVExtractorForStatus(DataExtractionRequest.Status status) {
        return null;
    }

    public boolean hasCSVExtractorForStatus(DataExtractionRequest.Status status) {
        return false;
    }

}