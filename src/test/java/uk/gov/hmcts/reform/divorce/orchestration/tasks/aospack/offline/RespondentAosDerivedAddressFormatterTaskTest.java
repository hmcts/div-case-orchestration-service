package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelperTest.EXPECTED_DERIVED_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelperTest.EXPECTED_DERIVED_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelperTest.buildCaseWithRespondentCorrespondenceAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelperTest.buildCaseWithRespondentHomeAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelperTest.buildCaseWithRespondentSolicitorAddress;

public class RespondentAosDerivedAddressFormatterTaskTest {

    private RespondentAosDerivedAddressFormatterTask classUnderTest;
    private TaskContext context;

    @Before
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        classUnderTest = new RespondentAosDerivedAddressFormatterTask();
    }

    @Test
    public void shouldReturnDataWithRespondentSolicitorAddress() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentSolicitorAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(D8_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(returnedData.get(D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS), is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));
    }

    @Test
    public void shouldReturnDataWithRespondentHomeAddress() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(D8_RESPONDENT_HOME_ADDRESS));
        assertThat(returnedData.get(D8_DERIVED_RESPONDENT_HOME_ADDRESS), is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));
    }

    @Test
    public void shouldReturnCorrectData_whenRespondentCorrespondenceAddressIsNotProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(D8_RESPONDENT_HOME_ADDRESS));
        assertThat(returnedData.get(D8_RESPONDENT_CORRESPONDENCE_ADDRESS), is(nullValue()));
        assertThat(returnedData.get(D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS), is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));
    }

    @Test
    public void shouldReturnCorrectData_whenRespondentCorrespondenceAddressIsProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentCorrespondenceAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(D8_RESPONDENT_CORRESPONDENCE_ADDRESS));
        assertThat(returnedData.get(D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS), is(EXPECTED_DERIVED_CORRESPONDENCE_ADDRESS));
    }

    @Test
    public void shouldSet_RespondentCorrespondenceUseHomeAddress_No_WhenRespondentCorrespondenceAddressIsProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentCorrespondenceAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData.get(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS), is(NO_VALUE));
    }

    @Test
    public void shouldSet_RespondentCorrespondenceUseHomeAddress_Yes_WhenRespondentCorrespondenceAddressIsNotProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData.get(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS), is(YES_VALUE));
    }

}