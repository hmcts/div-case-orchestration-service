package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;

public class CSVExtractorFactory {

    public CSVExtractor getCSVExtractorForStatus(DataExtractionRequest.Status status) {
        return null;
    }

    public boolean hasCSVExtractorForStatus(DataExtractionRequest.Status status) {
        return false;
    }
}
