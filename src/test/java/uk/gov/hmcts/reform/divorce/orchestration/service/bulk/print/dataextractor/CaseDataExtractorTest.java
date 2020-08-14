package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_TYPE_DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;

public class CaseDataExtractorTest {

    @Test
    public void getCaseReferenceShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        assertThat(CaseDataExtractor.getCaseReference(caseData), is(TEST_CASE_FAMILY_MAN_ID));
    }

    @Test
    public void getServiceApplicationRefusalReasonShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_REFUSAL_REASON,
            TEST_SERVICE_APPLICATION_REFUSAL_REASON);
        assertThat(CaseDataExtractor.getServiceApplicationRefusalReason(caseData), is(TEST_SERVICE_APPLICATION_REFUSAL_REASON));
    }

    @Test
    public void getServiceApplicationGrantedShouldReturnYesValue() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_GRANTED,
            TEST_SERVICE_APPLICATION_GRANTED);
        assertThat(CaseDataExtractor.getServiceApplicationGranted(caseData), is(TEST_SERVICE_APPLICATION_GRANTED));
    }

    @Test
    public void getServiceApplicationTypeShouldReturnDeemedValue() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_TYPE,
            TEST_SERVICE_APPLICATION_TYPE_DEEMED);
        assertThat(CaseDataExtractor.getServiceApplicationType(caseData), is(TEST_SERVICE_APPLICATION_TYPE_DEEMED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCaseReferenceShouldThrowInvalidData() {
        CaseDataExtractor.getCaseReference(EMPTY_MAP);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationRefusalReasonShouldThrowInvalidData() {
        CaseDataExtractor.getServiceApplicationRefusalReason(EMPTY_MAP);
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}