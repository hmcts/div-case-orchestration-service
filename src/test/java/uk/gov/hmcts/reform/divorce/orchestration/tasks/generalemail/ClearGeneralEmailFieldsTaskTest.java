package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;

public class ClearGeneralEmailFieldsTaskTest {

    public static final String TEST_VALUE = "test value";

    private ClearGeneralEmailFieldsTask clearGeneralEmailFieldsTask;

    @Test
    public void givenGeneralEmailFieldsPresent_whenTaskExecutes_thenTheFieldsAreRemoved() {
        clearGeneralEmailFieldsTask = new ClearGeneralEmailFieldsTask();

        List<String> generalEmailFields = asList(GENERAL_EMAIL_PARTIES, GENERAL_EMAIL_DETAILS, GENERAL_EMAIL_OTHER_RECIPIENT_NAME,
            GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL);

        Map<String, Object> payload = new HashMap<>();
        generalEmailFields.forEach(fieldName -> payload.put(fieldName, TEST_VALUE));

        Map<String, Object> result = clearGeneralEmailFieldsTask.execute(null, payload);
        generalEmailFields.forEach(fieldName -> assertThat(result.containsKey(fieldName), is(false)));
    }
}
