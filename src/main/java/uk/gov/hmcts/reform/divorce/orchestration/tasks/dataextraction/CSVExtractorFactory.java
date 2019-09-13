package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;

@Component
public class CSVExtractorFactory {

    @Autowired
    private DecreeAbsoluteDataExtractorStrategy decreeAbsoluteDataExtractorStrategy;

    private Map<Status, CSVExtractorStrategy> csvExtractorMap = new EnumMap<>(Status.class);

    @PostConstruct
    public void init() {
        csvExtractorMap.put(DA, decreeAbsoluteDataExtractorStrategy);
    }

    public CSVExtractorStrategy getCSVExtractorForStatus(Status status) {
        CSVExtractorStrategy csvExtractorStrategy = csvExtractorMap.get(status);

        if (csvExtractorStrategy == null) {
            throw new UnsupportedOperationException(format("CSVExtractor for %s not implemented.", status));
        }

        return csvExtractorStrategy;
    }

    public boolean hasCSVExtractorForStatus(Status status) {
        return csvExtractorMap.containsKey(status);
    }

}