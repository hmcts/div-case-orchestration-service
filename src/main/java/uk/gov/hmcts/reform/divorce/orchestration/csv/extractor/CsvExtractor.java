package uk.gov.hmcts.reform.divorce.orchestration.csv.extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CsvExtractor {

    BufferedWriter produceCvsFile(Set<Map<String, String>> Data) throws IOException;
}
