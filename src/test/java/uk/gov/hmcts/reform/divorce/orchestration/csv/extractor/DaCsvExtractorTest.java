package uk.gov.hmcts.reform.divorce.orchestration.csv.extractor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static uk.gov.hmcts.reform.divorce.orchestration.csv.extractor.DaCsvExtractor.*;


@RunWith(SpringRunner.class)
public class DaCsvExtractorTest {

    @Test
    public void testThatDataReturned_IsCsvFormat() throws IOException {

        Set<Map<String, String>> testData = new HashSet<>();
        testData.add(buildTestData(
                "LV17D90022",
                "11/05/2019",
                "13/05/2019",
                "respondent"));
        testData.add(buildTestData(
                "LV17D90021",
                "06/07/2019",
                "08/07/2019",
                "petitioner"));

        BufferedWriter csvFile = new DaCsvExtractor().produceCvsFile(testData);
    }

    private Map<String, String> buildTestData(
            String caseReference,
            String applicationDate,
            String pronouncementDate,
            String partyApplying) {

        Map<String, String> testData = new HashMap<>();

        testData.put(CASE_REFERENCE_NUMBER, caseReference);
        testData.put(DA_APPLICATION_DATE, applicationDate);
        testData.put(DN_PRONOUNCEMENT_DATE, pronouncementDate);
        testData.put(PARTY_APPLYING_FOR_DA, partyApplying);

        return testData;
    }

}
