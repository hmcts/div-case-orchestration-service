package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithCoRespondentNames;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithCoRespondentSolicitorNames;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithRespondentNames;

public class AddresseeDataExtractorTest {

    public static final String RESPONDENTS_ADDRESS = "123 Respondent Str\nRespondent\ncounty\nRE5 P0N";
    public static final String RESPONDENT_SOLICITORS_ADDRESS = "321 Resp Solicitor\ntown\ncounty\npostcode";
    public static final String RESPONDENT_SOLICITOR_REF = "SolRef4567";
    public static final String RESPONDENT_SOLICITORS_EXPECTED_NAME = "Sol" + TestConstants.TEST_RESPONDENT_FULL_NAME;
    public static final String CO_RESPONDENT_SOLICITORS_EXPECTED_NAME = "CoSol" + TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
    public static final String CO_RESPONDENT_ADDRESS = "456 CoRespondent Str\nCoRespondent\nCounty\nRE5 P0N";
    public static final String CO_RESPONDENT_SOLICITOR_ADDRESS = "456 CoRespondent Solicitor Str\nCoRespondent Solicitor\nCounty\nRE5 P0N";
    public static final String D8_CASE_REFERENCE = "LV17D80102";
    public static final String CO_RESPONDENT_SOLICITOR_REF = "SolRef1234";
    public static final String VALID_HEARING_DATE = "2020-10-20";

    @Test
    public void getRespondentShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondent();

        Addressee addressee = AddresseeDataExtractor.getRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(RESPONDENTS_ADDRESS));
        assertThat(addressee.getName(), is(TestConstants.TEST_RESPONDENT_FULL_NAME));
    }

    @Test
    public void getCoRespondentShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondent();

        Addressee addressee = AddresseeDataExtractor.getCoRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENT_ADDRESS));
        assertThat(addressee.getName(), is(TestConstants.TEST_CO_RESPONDENT_FULL_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCoRespondentShouldThrow_InvalidData() {
        Map<String, Object> caseData = ImmutableMap.of(D_8_PETITIONER_FIRST_NAME, "Finn");

        Addressee addressee = AddresseeDataExtractor.getCoRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENT_ADDRESS));
        assertThat(addressee.getName(), is(TestConstants.TEST_CO_RESPONDENT_FULL_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames();

        AddresseeDataExtractor.getRespondent(caseData);
    }

    @Test
    public void getRespondentSolicitorShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitor();

        Addressee addressee = AddresseeDataExtractor.getRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(RESPONDENT_SOLICITORS_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    @Test
    public void getCoRespondentSolicitorShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentSolicitor();

        Addressee addressee = AddresseeDataExtractor.getCoRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(addressee.getName(), is(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCoRespondentSolicitorShouldThrow_InvalidData() {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_SOLICITOR_REF, "TestData");

        Addressee addressee = AddresseeDataExtractor.getCoRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(addressee.getName(), is(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentSolicitorShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitor();

        AddresseeDataExtractor.getRespondent(caseData);
    }

    public static Map<String, Object> buildCaseDataWithRespondent() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames();
        caseData.put(RESPONDENT_ADDRESS, RESPONDENTS_ADDRESS);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithRespondentSolicitor() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames();

        caseData.put(RESPONDENT_SOLICITOR_NAME, RESPONDENT_SOLICITORS_EXPECTED_NAME);
        caseData.put(RESPONDENT_SOLICITOR_ADDRESS, RESPONDENT_SOLICITORS_ADDRESS);
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, RESPONDENT_SOLICITOR_REF);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithCoRespondent() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames();
        caseData.put(D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, CO_RESPONDENT_ADDRESS);
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(COSTS_CLAIM_GRANTED, YES_VALUE);
        caseData.put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
        caseData.put(D_8_CASE_REFERENCE, D8_CASE_REFERENCE);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, VALID_HEARING_DATE);
        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithCoRespondentSolicitor() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentSolicitorNames(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME);
        caseData.put(OrchestrationConstants.CO_RESPONDENT_SOLICITOR_ADDRESS, CO_RESPONDENT_SOLICITOR_ADDRESS);
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(COSTS_CLAIM_GRANTED, YES_VALUE);
        caseData.put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(D_8_CASE_REFERENCE, D8_CASE_REFERENCE);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, CO_RESPONDENT_SOLICITOR_REF);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, VALID_HEARING_DATE);
        return caseData;
    }
}
