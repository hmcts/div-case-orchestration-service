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
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

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
    private static final String ADR_COUNTY = "County";
    private static final String ADR_COUNTRY = "Country";
    private static final String ADR_TOWN = "PostTown";
    private static final String ADR_POSTCODE = "Postcode";
    private static final String CO_RESP = "CoResp-";
    private static final String SOL = "Sol-";

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
    public void shouldReturnDerivedSolicitorAddress_ForRepresentedCoRespondent() throws Exception {
        String derivedAddress = expectedDerivedAddress(CO_RESP, LINE_1, LINE_2, LINE_3,
            ADR_COUNTY, ADR_COUNTRY, ADR_TOWN, ADR_POSTCODE);
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
        String derivedAddress = expectedDerivedAddress(SOL, LINE_1, LINE_2, LINE_3,
            ADR_COUNTY, ADR_COUNTRY, ADR_TOWN, ADR_POSTCODE);
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

    private void addDataForRepresentedCoRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        Map<String, Object> coRespondentAddress = new HashMap<String, Object>() {
            {
                put(ADDRESS_LINE_1, appendPrefix(CO_RESP, LINE_1));
                put(ADDRESS_LINE_2, appendPrefix(CO_RESP, LINE_2));
                put(ADDRESS_LINE_3, appendPrefix(CO_RESP, LINE_3));
                put(COUNTY, appendPrefix(CO_RESP, ADR_COUNTY));
                put(COUNTRY, appendPrefix(CO_RESP, ADR_COUNTRY));
                put(POST_TOWN, appendPrefix(CO_RESP, ADR_TOWN));
                put(POST_CODE, appendPrefix(CO_RESP, ADR_POSTCODE));
            }
        };
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_CO_RESPONDENT_SOLICITOR_ADDRESS, coRespondentAddress);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
    }

    private void addDataForNonRepresentedCoRespondentToCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        Map<String, Object> coRespondentSolicitorAddress = new HashMap<String, Object>() {
            {
                put(ADDRESS_LINE_1, appendPrefix(SOL, LINE_1));
                put(ADDRESS_LINE_2, appendPrefix(SOL, LINE_2));
                put(ADDRESS_LINE_3, appendPrefix(SOL, LINE_3));
                put(COUNTY, appendPrefix(SOL, ADR_COUNTY));
                put(COUNTRY, appendPrefix(SOL, ADR_COUNTRY));
                put(POST_TOWN, appendPrefix(SOL, ADR_TOWN));
                put(POST_CODE, appendPrefix(SOL, ADR_POSTCODE));
            }
        };

        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, coRespondentSolicitorAddress);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
    }

    private String appendPrefix(String prefix, String value) {
        return prefix + value;
    }

    private String expectedDerivedAddress(String prefixValue, String... addressValues) {
        List<String> addressList = Arrays.asList(addressValues)
            .stream()
            .map(addressItem -> appendPrefix(prefixValue, addressItem))
            .collect(Collectors.toList());

        return String.join("\n", addressList);
    }
}