package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;

public class CaseDataExtractorTest {

    @Test
    public void getCaseReferenceShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        assertThat(CaseDataExtractor.getCaseReference(caseData), is(TEST_CASE_FAMILY_MAN_ID));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCaseReferenceShouldThrowInvalidDataForTaskException() {
        CaseDataExtractor.getCaseReference(Collections.emptyMap());
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}