package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@Component
public class CSVExtractorFactory {

    @Autowired
    private DecreeAbsoluteDataExtractor decreeAbsoluteDataExtractor;

    @Autowired
    private AOSDataExtractor aosDataExtractor;

    @Autowired
    private DecreeNisiDataExtractor decreeNisiDataExtractor;

    private Map<Status, CSVExtractor> csvExtractorMap = new EnumMap<>(Status.class);

    @PostConstruct
    public void init() {
        csvExtractorMap.put(DA, decreeAbsoluteDataExtractor);
        csvExtractorMap.put(AOS, aosDataExtractor);
        csvExtractorMap.put(DN, decreeNisiDataExtractor);
    }

    public CSVExtractor getCSVExtractorForStatus(Status status) {
        CSVExtractor csvExtractor = csvExtractorMap.get(status);

        if (csvExtractor == null) {
            throw new UnsupportedOperationException(format("CSVExtractor for %s not implemented.", status));
        }

        return csvExtractor;
    }

    public boolean hasCSVExtractorForStatus(Status status) {
        return csvExtractorMap.containsKey(status);
    }

}