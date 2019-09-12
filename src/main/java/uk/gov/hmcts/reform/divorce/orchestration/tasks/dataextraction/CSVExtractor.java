package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

public interface CSVExtractor {

    String getHeaderLine();

    String getDestinationEmailAddress();

    String getFileNamePrefix();

}
