package uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.BAILIFF_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class BailiffApplicationApprovedDataTaskTest {

    private BailiffApplicationApprovedDataTask bailiffApplicationApprovedDataTask;

    @Before
    public void setup() {
        bailiffApplicationApprovedDataTask = new BailiffApplicationApprovedDataTask();
    }

    @Test
    public void whenTaskExecuted_thenFieldsAreCopiedAsExpected() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_GRANTED, "anyValue");

        caseData = bailiffApplicationApprovedDataTask.execute(contextWithToken(), caseData);

        Object bailiffApplicationGranted = caseData.get(BAILIFF_APPLICATION_GRANTED);

        assertThat(bailiffApplicationGranted, is(notNullValue()));
        assertThat(bailiffApplicationGranted, is(caseData.get(SERVICE_APPLICATION_GRANTED)));
    }
}
