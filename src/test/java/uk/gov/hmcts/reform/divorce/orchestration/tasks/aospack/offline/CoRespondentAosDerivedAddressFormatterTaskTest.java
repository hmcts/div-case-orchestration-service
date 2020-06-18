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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS;

public class CoRespondentAosDerivedAddressFormatterTaskTest {

    private static final String ADDRESS_LINE_1 = "AddressLine1";
    private static final String ADDRESS_LINE_2 = "AddressLine2";
    private static final String ADDRESS_LINE_3 = "AddressLine3";
    private static final String COUNTY = "County";
    private static final String COUNTRY = "Country";
    private static final String POST_TOWN = "PostTown";
    private static final String POST_CODE = "PostCode";
    private static final String ADDY_LINE_1 = "CoRespAddyLine1";
    private static final String ADDY_LINE_2 = "CoRespAddyLine2";
    private static final String ADDY_LINE_3 = "CoRespAddyLine3";
    private static final String ADDY_COUNTY = "CoRespCounty";
    private static final String ADDY_COUNTRY = "CoRespCountry";
    private static final String ADDY_POST_TOWN = "CoRespPostTown";
    private static final String ADDY_POSTCODE = "CoRespPostcode";
    private static final String EXPECTED_DERIVED_ADDRESS = "CoRespAddyLine1\nCoRespAddyLine2\nCoRespAddyLine3\nCoRespCounty\n"
        + "CoRespCountry\nCoRespPostTown\nCoRespPostcode";

    private static Map<String, Object> BASE_ADDRESS;

    private CoRespondentAosDerivedAddressFormatterTask classUnderTest;
    private TaskContext context;

    @Before
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        classUnderTest = new CoRespondentAosDerivedAddressFormatterTask();
        BASE_ADDRESS = new HashMap<String, Object>() {
            {
                put(ADDRESS_LINE_1, ADDY_LINE_1);
                put(ADDRESS_LINE_2, ADDY_LINE_2);
                put(ADDRESS_LINE_3, ADDY_LINE_3);
                put(COUNTY, ADDY_COUNTY);
                put(COUNTRY, ADDY_COUNTRY);
                put(POST_TOWN, ADDY_POST_TOWN);
                put(POST_CODE, ADDY_POSTCODE);
            }
        };
    }

    @Test
    public void shouldReturnDataWithCoRespondentSolicitorAddress() throws TaskException {
        Map<String, Object> caseData = buildCaseWithCoRespondentSolicitorAddress();

        Map<String, Object> returnedData = classUnderTest.execute(context, caseData);

        assertThat(returnedData, hasKey(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(returnedData.get(CO_RESPONDENT_SOLICITOR_ADDRESS), is(EXPECTED_DERIVED_ADDRESS));
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
        assertThat(returnedData.get(CO_RESPONDENT_ADDRESS), is(EXPECTED_DERIVED_ADDRESS));
    }

    @Test
    public void formatDerivedCoRespondentSolicitorAddress() {
        Map<String, Object> caseData = buildCaseWithCoRespondentSolicitorAddress();

        String derivedCoRespondentSolicitorAddr = classUnderTest.formatDerivedCoRespondentSolicitorAddress(caseData);

        assertThat(derivedCoRespondentSolicitorAddr, is(EXPECTED_DERIVED_ADDRESS));

    }

    @Test
    public void formatDerivedReasonForDivorceAdultery3rdAddress() {
        Map<String, Object> caseData = buildCaseWithCoRespondentAddress();

        String derivedCoRespondentAddr = classUnderTest.formatDerivedReasonForDivorceAdultery3rdAddress(caseData);

        assertThat(derivedCoRespondentAddr, is(EXPECTED_DERIVED_ADDRESS));
    }

    private Map<String, Object> buildCaseWithCoRespondentSolicitorAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_CO_RESPONDENT_SOLICITOR_ADDRESS, BASE_ADDRESS);
                put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
            }
        };
    }

    private Map<String, Object> buildCaseWithCoRespondentAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, BASE_ADDRESS);
                put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
            }
        };
    }

}