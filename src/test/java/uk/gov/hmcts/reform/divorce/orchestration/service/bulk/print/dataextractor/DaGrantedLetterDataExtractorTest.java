package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE;

public class DaGrantedLetterDataExtractorTest {

    private static final String VALID_DATE_FROM_CCD = "2010-10-01";
    private static final String EXPECTED_DATE = "1 October 2010";

    @Test
    public void getDaGrantedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithDaGrantedDate(VALID_DATE_FROM_CCD);
        assertThat(DaGrantedLetterDataExtractor.getDaGrantedDate(caseData), is(EXPECTED_DATE));
    }

    @Test
    public void getDaGrantedDateThrowsExceptions() {
        asList("", null).forEach(daDateValue -> {
            try {
                DaGrantedLetterDataExtractor.getDaGrantedDate(buildCaseDataWithDaGrantedDate(daDateValue));
                fail("Should have thrown exception");
            } catch (InvalidDataForTaskException e) {
                thisTestPassed();
            }
        });
    }

    private static Map<String, Object> buildCaseDataWithDaGrantedDate(String data) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DA_GRANTED_DATE, data);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }
}
