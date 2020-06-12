package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithCoRespondentNames;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithRespondentNames;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithRespondentSolicitorNames;

public class AddresseeDataExtractorTest {

    public static String RESPONDENTS_ADDRESS = "123 Respondent Str\nRespondent\ncounty\nRE5 P0N";
    public static String RESPONDENT_SOLICITORS_ADDRESS = "321 Resp Solicitor\ntown\ncounty\npostcode";
    public static String RESPONDENTS_EXPECTED_NAME = FIRST_NAME + " " + LAST_NAME;
    public static String RESPONDENT_SOLICITORS_EXPECTED_NAME = "Sol" + FIRST_NAME + " " + LAST_NAME;

    public static String CO_RESPONDENTS_ADDRESS = "456 CoRespondent Str\nCoRespondent\ncounty\nRE5 P0N";
    public static String CO_RESPONDENT_SOLICITORS_ADDRESS = "654 CoResp Solicitor\ntown\ncounty\npostcode";
    public static String CO_RESPONDENTS_EXPECTED_NAME = FIRST_NAME + " " + LAST_NAME;
    public static String CO_RESPONDENT_SOLICITORS_EXPECTED_NAME = "SolCoRes" + FIRST_NAME + " " + LAST_NAME;

    @Test
    public void getRespondentShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondentAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(RESPONDENTS_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENTS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        AddresseeDataExtractor.getRespondent(caseData);
    }

    @Test
    public void getRespondentSolicitorShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(RESPONDENT_SOLICITORS_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentSolicitorShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorNames(RESPONDENTS_EXPECTED_NAME);

        AddresseeDataExtractor.getRespondent(caseData);
    }

    @Test
    public void getCoRespondentShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getCoRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENTS_ADDRESS));
        assertThat(addressee.getName(), is(CO_RESPONDENTS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCoRespondentShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        AddresseeDataExtractor.getCoRespondent(caseData);
    }

    @Test
    public void getCoRespondentSolicitorShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentSolicitorAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getCoRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(CO_RESPONDENT_SOLICITORS_ADDRESS));
        assertThat(addressee.getName(), is(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCoRespondentSolicitorShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorNames(CO_RESPONDENTS_EXPECTED_NAME);

        AddresseeDataExtractor.getRespondent(caseData);
    }

    public static Map<String, Object> buildCaseDataWithRespondentAsAddressee() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);
        caseData.put(RESPONDENT_ADDRESS, RESPONDENTS_ADDRESS);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithRespondentSolicitorAsAddressee() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorNames(RESPONDENT_SOLICITORS_EXPECTED_NAME);
        caseData.put(RESPONDENT_SOLICITOR_ADDRESS, RESPONDENT_SOLICITORS_ADDRESS);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithCoRespondentAsAddressee() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);
        caseData.put(CO_RESPONDENT_ADDRESS, CO_RESPONDENTS_ADDRESS);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithCoRespondentSolicitorAsAddressee() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CO_RESPONDENT_SOLICITOR_NAME, CO_RESPONDENT_SOLICITORS_EXPECTED_NAME);
        caseData.put(CO_RESPONDENT_SOLICITOR_ADDRESS, CO_RESPONDENT_SOLICITORS_ADDRESS);
        caseData.put(CO_RESPONDENT_REPRESENTED, YES_VALUE);

        return caseData;
    }
}
