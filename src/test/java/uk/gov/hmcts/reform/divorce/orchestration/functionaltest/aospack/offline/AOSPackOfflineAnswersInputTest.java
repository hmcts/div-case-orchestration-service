package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_SOLICITOR_ADDRESS;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class AOSPackOfflineAnswersInputTest {

    private static final String CCD_RESPONSE_DATA_FIELD = "data";
    private static final String ADDRESS_LINE_1 = "AddressLine1";
    private static final String ADDRESS_LINE_2 = "AddressLine2";
    private static final String ADDRESS_LINE_3 = "AddressLine3";
    private static final String COUNTY = "County";
    private static final String COUNTRY = "Country";
    private static final String POST_TOWN = "PostTown";
    private static final String POST_CODE = "PostCode";

    private static final String LINE_1 = "AddyLine1";
    private static final String LINE_2 = "AddyLine2";
    private static final String LINE_3 = "AddyLine3";
    private static final String ADY_COUNTY = "County";
    private static final String ADY = "Country";
    private static final String ADY_TOWN = "PostTown";
    private static final String ADY_POSTCODE = "Postcode";
    private static final String CO_RESP_PREFIX = "CoResp-";
    private static final String SOLICITOR_PREFIX = "Sol-";
    private static final String RESP_SOLICITOR_PREFIX = "RespSol-";
    private static final String RESP_PREFIX = "Resp-";
    private static final String RESP_CORRESPONDENCE_PREFIX = "RespCoresp-";
    private static final String TEST_REASON_FOR_DIVORCE = "desertion";

    @Autowired
    private MockMvc mockMvc;

    private CcdCallbackRequest ccdCallbackRequest;

    @Before
    public void setUp() throws Exception {
        ccdCallbackRequest = getJsonFromResourceFile("/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFields_ForRespondent_ForAdultery() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_AOS_ADMIT_ADULTERY, YES_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", "respondent")
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(notNullValue())),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_ADMIT_OR_CONSENT_TO_FACT, is(YES_VALUE))
                ))
            )));
    }

    @Test
    public void shouldReturnOnlyAutomaticFields_ForCoRespondent_ForAdultery() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", CO_RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasNoJsonPath(STATE_CCD_FIELD),
                    hasNoJsonPath(RECEIVED_AOS_FROM_RESP)
                ))
            )));
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFieldsIfDefended_ForUnreasonableBehaviour() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(AOS_SUBMITTED_AWAITING_ANSWER)),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_WILL_DEFEND_DIVORCE, is(YES_VALUE))
                ))
            )));
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFieldsIfUndefended_ForUnreasonableBehaviour() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(AWAITING_DECREE_NISI)),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_WILL_DEFEND_DIVORCE, is(NO_VALUE))
                ))
            )));
    }

    @Test
    public void shouldReturnDerivedSolicitorAddress_ForRepresentedRespondent() throws Exception {
        String derivedAddress = expectedDerivedAddress(RESP_SOLICITOR_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataForRepresentedRespondentToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.respondentSolicitorRepresented", is(YES_VALUE)),
                hasJsonPath("$.data.D8RespondentSolicitorAddress"),
                hasJsonPath("$.data.D8DerivedRespondentSolicitorAddr", is(derivedAddress)),
                hasNoJsonPath("$.data.D8DerivedRespondentHomeAddress")
            )));
    }

    @Test
    public void shouldReturnDerivedRespondentHomeAddress_ForNonRepresentedRespondent() throws Exception {
        String derivedAddress = expectedDerivedAddress(RESP_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataForNonRepresentedRespondentToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.respondentSolicitorRepresented", is(NO_VALUE)),
                hasJsonPath("$.data.D8RespondentHomeAddress"),
                hasJsonPath("$.data.D8DerivedRespondentHomeAddress", is(derivedAddress)),
                hasNoJsonPath("$.data.D8DerivedRespondentSolicitorAddr")
            )));
    }

    @Test
    public void shouldReturnDerivedRespondentCorrespondenceAddress_When_RespondentCorrespondenceAddressIsProvided() throws Exception {
        String derivedAddress = expectedDerivedAddress(RESP_CORRESPONDENCE_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataWithRespondentCorrespondenceAddressToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.D8RespondentCorrespondenceAddress"),
                hasJsonPath("$.data.D8DerivedRespondentCorrespondenceAddr", is(derivedAddress))
            )));
    }

    @Test
    public void shouldUseRespondentHomeAddress_When_RespondentCorrespondenceAddressIsNotProvided() throws Exception {
        String derivedAddress = expectedDerivedAddress(RESP_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataWithRespondentHomeAddressToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.D8RespondentHomeAddress"),
                hasJsonPath("$.data.D8DerivedRespondentCorrespondenceAddr", is(derivedAddress))
            )));
    }

    @Test
    public void shouldReturnDerivedSolicitorAddress_ForRepresentedCoRespondent() throws Exception {
        String derivedAddress = expectedDerivedAddress(CO_RESP_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataForRepresentedCoRespondentToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", CO_RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.CoRespondentSolicitorRepresented", is(YES_VALUE)),
                hasJsonPath("$.data.DerivedCoRespondentSolicitorAddr", is(derivedAddress)),
                hasNoJsonPath("$.data.D8DerivedReasonForDivorceAdultery3rdAddr")
            )));
    }

    @Test
    public void shouldReturnDerivedCoRespondentAddress_ForNonRepresentedCoRespondent() throws Exception {
        String derivedAddress = expectedDerivedAddress(SOLICITOR_PREFIX, LINE_1, LINE_2, LINE_3,
            ADY_COUNTY, ADY, ADY_TOWN, ADY_POSTCODE);
        addDataForNonRepresentedCoRespondentToCallbackRequest(ccdCallbackRequest);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", CO_RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.CoRespondentSolicitorRepresented", is(NO_VALUE)),
                hasJsonPath("$.data.D8DerivedReasonForDivorceAdultery3rdAddr", is(derivedAddress)),
                hasNoJsonPath("$.data.DerivedCoRespondentSolicitorAddr")
            )));
    }

    @Test
    public void shouldReturnEmptyDerivedCoRespondentAddress_WhenInvalidDataIsProvided() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, YES_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", CO_RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.CoRespondentSolicitorRepresented", is(YES_VALUE)),
                hasJsonPath("$.data.DerivedCoRespondentSolicitorAddr", is(nullValue()))
            )));
    }

    private void addDataForRepresentedRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_RESPONDENT_SOLICITOR_ADDRESS, buildAddress(RESP_SOLICITOR_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_SOL_REPRESENTED, YES_VALUE);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, TEST_REASON_FOR_DIVORCE);

    }

    private void addDataForNonRepresentedRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_RESPONDENT_HOME_ADDRESS, buildAddress(RESP_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_SOL_REPRESENTED, NO_VALUE);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, TEST_REASON_FOR_DIVORCE);
    }

    private void addDataWithRespondentCorrespondenceAddressToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_RESPONDENT_CORRESPONDENCE_ADDRESS, buildAddress(RESP_CORRESPONDENCE_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, TEST_REASON_FOR_DIVORCE);
    }

    private void addDataWithRespondentHomeAddressToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_RESPONDENT_HOME_ADDRESS, buildAddress(RESP_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, TEST_REASON_FOR_DIVORCE);
    }

    private void addDataForRepresentedCoRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_SOLICITOR_ADDRESS, buildAddress(CO_RESP_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
    }

    private void addDataForNonRepresentedCoRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) { //TODO refactor, pass prefix as parameter
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, buildAddress(SOLICITOR_PREFIX));
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
    }

    private HashMap<String, Object> buildAddress(String prefix) {
        return new HashMap<String, Object>() {
            {
                put(ADDRESS_LINE_1, prependPrefix(prefix, LINE_1));
                put(ADDRESS_LINE_2, prependPrefix(prefix, LINE_2));
                put(ADDRESS_LINE_3, prependPrefix(prefix, LINE_3));
                put(COUNTY, prependPrefix(prefix, ADY_COUNTY));
                put(COUNTRY, prependPrefix(prefix, ADY));
                put(POST_TOWN, prependPrefix(prefix, ADY_TOWN));
                put(POST_CODE, prependPrefix(prefix, ADY_POSTCODE));
            }
        };
    }

    private String prependPrefix(String prefix, String value) {
        return prefix + value;
    }

    private String expectedDerivedAddress(String prefixValue, String... addressValues) {
        List<String> addressList = Arrays.asList(addressValues)
            .stream()
            .map(addressItem -> prependPrefix(prefixValue, addressItem))
            .collect(Collectors.toList());

        return String.join("\n", addressList);
    }
}