package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;

public class CaseDataExtractorTest {

    @Test
    public void getCaseReferenceShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithField(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        assertThat(CaseDataExtractor.getCaseReference(caseData), is(TEST_CASE_FAMILY_MAN_ID));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCaseReferenceShouldThrowInvalidDat() {
        CaseDataExtractor.getCaseReference(EMPTY_MAP);
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}