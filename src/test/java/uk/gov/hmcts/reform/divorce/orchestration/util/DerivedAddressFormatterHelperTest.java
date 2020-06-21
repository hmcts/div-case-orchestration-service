package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_RESPONDENT_SOLICITOR_ADDRESS;

public class DerivedAddressFormatterHelperTest {

    public static final String ADDRESS_LINE_1 = "AddressLine1";
    public static final String ADDRESS_LINE_2 = "AddressLine2";
    public static final String ADDRESS_LINE_3 = "AddressLine3";
    public static final String COUNTY = "County";
    public static final String COUNTRY = "Country";
    public static final String POST_TOWN = "PostTown";
    public static final String POST_CODE = "PostCode";

    private static final String ADDY_LINE_1 = "AddyLine1";
    private static final String ADDY_LINE_2 = "AddyLine2";
    private static final String ADDY_LINE_3 = "AddyLine3";
    private static final String ADDY_COUNTY = "County";
    private static final String ADDY_COUNTRY = "Country";
    private static final String ADDY_POST_TOWN = "PostTown";
    private static final String ADDY_POSTCODE = "Postcode";

    private static final String CO_RESPONDENT_PREFIX = "CoResp";
    private static final String RESPONDENT_PREFIX = "Resp";
    private static final String RESPONDENT_CORRESPONDENCE_PREFIX = "RespCoresp";

    public static final String EXPECTED_DERIVED_CORESPONDENT_ADDRESS = "CoRespAddyLine1\nCoRespAddyLine2\nCoRespAddyLine3\nCoRespCounty\n"
        + "CoRespCountry\nCoRespPostTown\nCoRespPostcode";

    public static final String EXPECTED_DERIVED_RESPONDENT_ADDRESS = "RespAddyLine1\nRespAddyLine2\nRespAddyLine3\nRespCounty\n"
        + "RespCountry\nRespPostTown\nRespPostcode";

    public static final String EXPECTED_DERIVED_CORRESPONDENCE_ADDRESS = "RespCorespAddyLine1\nRespCorespAddyLine2\nRespCorespAddyLine3\n"
        + "RespCorespCounty\nRespCorespCountry\nRespCorespPostTown\nRespCorespPostcode";

    private static final String TEST_PERFIX = "any-";
    private static final String TEST_ADDR_PROPERTY = "any";
    private static final int MAX_EXPECTED_NEWLINES = 6;
    private static final String TEST_EXPECTED_LINE_1 = "any-AddyLine1";
    private static final String TEST_EXPECTED_LINE_2 = "any-AddyLine2";
    private static final String TEST_EXPECTED_COUNTY = "any-County";
    private static final String TEST_EXPECTED_POSTCODE = "any-Postcode";

    @Test
    public void formatDerivedCoRespondentSolicitorAddress() {
        Map<String, Object> caseData = buildCaseWithCoRespondentSolicitorAddress();

        String derivedCoRespondentSolicitorAddr = DerivedAddressFormatterHelper.formatDerivedCoRespondentSolicitorAddress(caseData);

        assertThat(derivedCoRespondentSolicitorAddr, is(EXPECTED_DERIVED_CORESPONDENT_ADDRESS));

    }

    @Test
    public void formatDerivedReasonForDivorceAdultery3rdAddress() {
        Map<String, Object> caseData = buildCaseWithCoRespondentAddress();

        String derivedCoRespondentAddr = DerivedAddressFormatterHelper.formatDerivedReasonForDivorceAdultery3rdAddress(caseData);

        assertThat(derivedCoRespondentAddr, is(EXPECTED_DERIVED_CORESPONDENT_ADDRESS));
    }

    @Test
    public void formatDerivedRespondentSolicitorAddress() {
        Map<String, Object> caseData = buildCaseWithRespondentSolicitorAddress();

        String derivedRespondentSolicitorAddr = DerivedAddressFormatterHelper.formatDerivedRespondentSolicitorAddress(caseData);

        assertThat(derivedRespondentSolicitorAddr, is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));

    }

    @Test
    public void formatDerivedRespondentHomeAddress() {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        String derivedRespondentHomeAddr = DerivedAddressFormatterHelper.formatDerivedRespondentHomeAddress(caseData);

        assertThat(derivedRespondentHomeAddr, is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));

    }

    @Test
    public void formatDerivedRespondentCorrespondenceAddress() {
        Map<String, Object> caseData = buildCaseWithRespondentCorrespondenceAddress();

        String derivedRespondentHomeAddr = DerivedAddressFormatterHelper.formatDerivedRespondentCorrespondenceAddress(caseData);

        assertThat(derivedRespondentHomeAddr, is(EXPECTED_DERIVED_CORRESPONDENCE_ADDRESS));
    }

    @Test
    public void shouldFormatHomeAddressWhenCorrespondenceAddressIsNotPopulated() {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        String derivedRespondentHomeAddr = DerivedAddressFormatterHelper.formatDerivedRespondentCorrespondenceAddress(caseData);

        assertThat(derivedRespondentHomeAddr, is(EXPECTED_DERIVED_RESPONDENT_ADDRESS));
    }

    @Test
    public void shouldReturnTrueIfD8RespondentCorrespondenceAddressIsPopulated() {
        Map<String, Object> caseData = buildCaseWithRespondentCorrespondenceAddress();

        boolean value = DerivedAddressFormatterHelper.isRespondentCorrespondenceAddressPopulated(caseData);

        assertThat(value, is(true));
    }

    @Test
    public void shouldReturnFalseIfD8RespondentCorrespondenceAddressNotPopulated() {
        Map<String, Object> caseData = buildCaseWithRespondentHomeAddress();

        boolean value = DerivedAddressFormatterHelper.isRespondentCorrespondenceAddressPopulated(caseData);

        assertThat(value, is(false));
    }

    @Test
    public void givenCcdAddressType_shouldFormatAddressWithNewLines() {
        Map<String, Object> addressType = buildAddress(TEST_PERFIX);
        Map<String, Object> address = new HashMap<>();
        address.put(TEST_ADDR_PROPERTY, addressType);

        String result = DerivedAddressFormatterHelper.formatToDerivedAddress(address, TEST_ADDR_PROPERTY);

        assertThat(result, is(notNullValue()));
        assertThat(result, containsString(TEST_EXPECTED_LINE_1));
        assertThat(StringUtils.countMatches(result, "\n"), is(MAX_EXPECTED_NEWLINES));
    }

    @Test
    public void givenNoValidCcdAddressType_shouldReturnNull() {
        Map<String, Object> address = new HashMap<>();

        String result = DerivedAddressFormatterHelper.formatToDerivedAddress(address, TEST_ADDR_PROPERTY);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldNotReturnExtraLinesWhenIncompleteAddressDataIsProvided() {
        Map<String, Object> inCompleteAddress = buildInCompleteAddress();

        Map<String, Object> address = new HashMap<>();
        address.put(TEST_ADDR_PROPERTY, inCompleteAddress);

        String result = DerivedAddressFormatterHelper.formatToDerivedAddress(address, TEST_ADDR_PROPERTY);

        assertThat(result, stringContainsInOrder(
            Arrays.asList(TEST_EXPECTED_LINE_1,
                TEST_EXPECTED_LINE_2,
                TEST_EXPECTED_COUNTY,
                TEST_EXPECTED_POSTCODE))
        );
    }

    public static Map<String, Object> buildCaseWithRespondentSolicitorAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_RESPONDENT_SOLICITOR_ADDRESS, buildAddress(RESPONDENT_PREFIX));
                put(RESP_SOL_REPRESENTED, YES_VALUE);
            }
        };
    }

    public static Map<String, Object> buildCaseWithRespondentHomeAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_RESPONDENT_HOME_ADDRESS, buildAddress(RESPONDENT_PREFIX));
                put(RESP_SOL_REPRESENTED, NO_VALUE);
            }
        };
    }

    public static Map<String, Object> buildCaseWithCoRespondentSolicitorAddress() {
        return new HashMap<String, Object>() {
            {
                put(CO_RESPONDENT_SOLICITOR_ADDRESS, buildAddress(CO_RESPONDENT_PREFIX));
                put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
            }
        };
    }

    public static Map<String, Object> buildCaseWithCoRespondentAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, buildAddress(CO_RESPONDENT_PREFIX));
                put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
            }
        };
    }

    public static Map<String, Object> buildCaseWithRespondentCorrespondenceAddress() {
        return new HashMap<String, Object>() {
            {
                put(D8_RESPONDENT_CORRESPONDENCE_ADDRESS, buildAddress(RESPONDENT_CORRESPONDENCE_PREFIX));
            }
        };
    }

    private static Map<String, Object> buildAddress(String prefix) {
        return new HashMap<String, Object>() {
            {
                put(ADDRESS_LINE_1, appendPrefix(prefix, ADDY_LINE_1));
                put(ADDRESS_LINE_2, appendPrefix(prefix, ADDY_LINE_2));
                put(ADDRESS_LINE_3, appendPrefix(prefix, ADDY_LINE_3));
                put(COUNTY, appendPrefix(prefix, ADDY_COUNTY));
                put(COUNTRY, appendPrefix(prefix, ADDY_COUNTRY));
                put(POST_TOWN, appendPrefix(prefix, ADDY_POST_TOWN));
                put(POST_CODE, appendPrefix(prefix, ADDY_POSTCODE));
            }
        };
    }

    private Map<String, Object> buildInCompleteAddress() {
        Map<String, Object> addressType = buildAddress(TEST_PERFIX);
        addressType.remove(ADDRESS_LINE_3);
        addressType.remove(POST_TOWN);
        addressType.remove(ADDY_COUNTRY);
        return addressType;
    }

    private static String appendPrefix(String prefix, String value) {
        return prefix + value;
    }

}