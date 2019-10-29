package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class FormFieldValuesToCoreFieldsRelayTest {

    private static final String RESP_AOS_2_YR_CONSENT = "RespAOS2yrConsent";
    private static final String RESP_AOS_ADULTERY = "RespAOSAdultery";
    private static final String RESP_ADMIT_OR_CONSENT_TO_FACT = "RespAdmitOrConsentToFact";
    private static final String UI_ONLY_RESP_WILL_DEFEND_DIVORCE = "UiOnly_RespWillDefendDivorce";
    private static final String RESP_WILL_DEFEND_DIVORCE = "RespWillDefendDivorce";

    private static final String TEST_VALUE = "testValue";

    private FormFieldValuesToCoreFieldsRelay classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new FormFieldValuesToCoreFieldsRelay();
    }

    @Test
    public void shouldRelayTwoYearsConsentValueToCoreField() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap(RESP_AOS_2_YR_CONSENT, TEST_VALUE));

        assertThat(returnedPayload, hasEntry(RESP_ADMIT_OR_CONSENT_TO_FACT, TEST_VALUE));
    }

    @Test
    public void shouldRelayAdulteryAdmissionValueToCoreField() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap(RESP_AOS_ADULTERY, TEST_VALUE));

        assertThat(returnedPayload, hasEntry(RESP_ADMIT_OR_CONSENT_TO_FACT, TEST_VALUE));
    }

    @Test
    public void shouldRelayWillDefendDivorceValueToCoreField() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap(UI_ONLY_RESP_WILL_DEFEND_DIVORCE, TEST_VALUE));

        assertThat(returnedPayload, hasEntry(RESP_WILL_DEFEND_DIVORCE, TEST_VALUE));
    }

    @Test
    public void shouldNotRelayAnything_IfNothingIsInPayload() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, emptyMap());

        assertThat(returnedPayload, is(emptyMap()));
    }

}