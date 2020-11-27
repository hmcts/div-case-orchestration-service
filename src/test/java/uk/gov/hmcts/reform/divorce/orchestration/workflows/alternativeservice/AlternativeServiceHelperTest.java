package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceHelper.isServedByAlternativeMethod;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceHelper.isServedByProcessServer;

public class AlternativeServiceHelperTest {

    @Test
    public void isServedByAlternativeMethodShouldReturnFalse() {
        assertThat(isServedByAlternativeMethod(new HashMap<>()), is(false));
        assertThat(isServedByAlternativeMethod(
            buildCaseData(SERVED_BY_ALTERNATIVE_METHOD, NO_VALUE)), is(false)
        );
        assertThat(isServedByAlternativeMethod(
            buildCaseData(SERVED_BY_ALTERNATIVE_METHOD, null)), is(false)
        );
    }

    @Test
    public void isServedByAlternativeMethodShouldReturnTrue() {
        assertThat(isServedByAlternativeMethod(
            buildCaseData(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)), is(true)
        );
    }

    @Test
    public void isServedByProcessServerShouldReturnFalse() {
        assertThat(isServedByAlternativeMethod(new HashMap<>()), is(false));
        assertThat(isServedByProcessServer(
            buildCaseData(SERVED_BY_PROCESS_SERVER, NO_VALUE)), is(false)
        );
        assertThat(isServedByProcessServer(
            buildCaseData(SERVED_BY_PROCESS_SERVER, null)), is(false)
        );
    }

    @Test
    public void isServedByProcessServerShouldReturnTrue() {
        assertThat(isServedByProcessServer(
            buildCaseData(SERVED_BY_PROCESS_SERVER, YES_VALUE)), is(true)
        );
    }

    private Map<String, Object> buildCaseData(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}
