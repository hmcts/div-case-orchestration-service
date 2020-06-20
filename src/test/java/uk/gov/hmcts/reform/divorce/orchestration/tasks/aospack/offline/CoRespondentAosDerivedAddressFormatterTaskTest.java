package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelperTest.EXPECTED_DERIVED_CORESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelperTest.buildCaseWithCoRespondentAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelperTest.buildCaseWithCoRespondentSolicitorAddress;

public class CoRespondentAosDerivedAddressFormatterTaskTest {

    private CoRespondentAosDerivedAddressFormatterTask classUnderTest;
    private TaskContext context;

    @Before
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        classUnderTest = new CoRespondentAosDerivedAddressFormatterTask();
    }

    @Test
    public void shouldReturnDataWithCoRespondentSolicitorAddress() throws TaskException {
        Map<String, Object> caseData = buildCaseWithCoRespondentSolicitorAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(returnedData.get(CO_RESPONDENT_SOLICITOR_ADDRESS), is(EXPECTED_DERIVED_CORESPONDENT_ADDRESS));
    }

    @Test
    public void shouldReturnNullWhenInvalidDataProvided() throws TaskException {
        Map<String, Object> caseData = new HashMap() {
            {
                put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
            }
        };

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(returnedData.get(CO_RESPONDENT_SOLICITOR_ADDRESS), is(nullValue()));
    }

    @Test
    public void shouldReturnDataWithCoRespondentAddress() throws TaskException {
        Map<String, Object> caseData = buildCaseWithCoRespondentAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(CO_RESPONDENT_ADDRESS));
        assertThat(returnedData.get(CO_RESPONDENT_ADDRESS), is(EXPECTED_DERIVED_CORESPONDENT_ADDRESS));
    }

}