package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class UpdateBulkScanITest {

    private static final String UPDATE_URL = "/update-case";

    private static final String AOS_OFFLINE_2_YEAR_SEP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/2yearSeparation/aosOffline2yrSep.json";
    private static final String INVALID_AOS_OFFLINE_2_YEAR_SEP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/2yearSeparation/invalidAosOffline2yrSep.json";

    private static final String AOS_OFFLINE_5_YEAR_SEP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/5yearSeparation/aosOffline5yrSep.json";
    private static final String INVALID_AOS_OFFLINE_5_YEAR_SEP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/5yearSeparation/invalidAosOffline5yrSep.json";

    private static final String AOS_OFFLINE_BEHAVIOUR_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Behaviour/aosOfflineUnreasonableBehaviour.json";
    private static final String INVALID_AOS_OFFLINE_BEHAVIOUR_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Behaviour/invalidAosOfflineUnreasonableBehaviour.json";

    private static final String AOS_OFFLINE_DESERTION_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Desertion/aosOfflineDesertion.json";
    private static final String INVALID_AOS_OFFLINE_DESERTION_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Desertion/invalidAosOfflineDesertion.json";

    private static final String AOS_OFFLINE_ADULTERY_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Adultery/aosOfflineAdultery.json";
    private static final String INVALID_AOS_OFFLINE_ADULTERY_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/Adultery/invalidAosOfflineAdultery.json";

    private static final String AOS_OFFLINE_ADULTERY_CO_RESP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/AdulteryCoResp/aosOfflineAdulteryCoResp.json";
    private static final String INVALID_AOS_OFFLINE_ADULTERY_CO_RESP_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/AdulteryCoResp/invalidAosOfflineAdulteryCoResp.json";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @Before
    public void setup() {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_2_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_2_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, "")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataFor2YrSep() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_2_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointFor2YrSep() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_2_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    mandatoryFieldIsMissing("RespLegalProceedingsExist"),
                    mandatoryFieldIsMissing("RespConsiderFinancialSituation"),
                    notAValidDate("RespStatementofTruthSignedDate"),
                    mustBeYesOrNo("RespAOS2yrConsent")
                ))
            ))
        ));
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataFor5YrSep() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_5_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointFor5YrSep() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_5_YEAR_SEP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    mandatoryFieldIsMissing("RespLegalProceedingsExist"),
                    mandatoryFieldIsMissing("RespConsiderFinancialSituation"),
                    notAValidDate("RespStatementofTruthSignedDate"),
                    mustBeYesOrNo("RespHardshipDefenseResponse")
                ))
            ))
        ));
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataForBehaviour() throws Exception {

        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_BEHAVIOUR_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointForBehaviour() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_BEHAVIOUR_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", allOf(hasItems(
                    "AOSReasonForDivorce must be \"Unreasonable behaviour\" or \"Desertion\"",
                    "RespWillDefendDivorce must be \"Proceed\", \"Defend\" or \"NoNoAdmission\"",
                    notAValidDate("RespStatementofTruthSignedDate")
                )))
            ))
        ));
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataForDesertion() throws Exception {

        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_DESERTION_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointForDesertion() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_DESERTION_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", allOf(hasItems(
                    "AOSReasonForDivorce must be \"Unreasonable behaviour\" or \"Desertion\"",
                    "RespWillDefendDivorce must be \"Proceed\", \"Defend\" or \"NoNoAdmission\"",
                    notAValidDate("RespStatementofTruthSignedDate")
                )))
            ))
        ));
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataForAdultery() throws Exception {

        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_ADULTERY_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointForAdultery() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_ADULTERY_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", allOf(hasItems(
                    mustBeYesOrNo("RespAOSAdultery"),
                    "AOSReasonForDivorce must be \"Adultery\"",
                    mandatoryFieldIsMissing("RespLegalProceedingsExist"),
                    notAValidDate("RespStatementofTruthSignedDate")
                )))
            ))
        ));
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidDataForAdulteryCoResp() throws Exception {

        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(AOS_OFFLINE_ADULTERY_CO_RESP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForUpdateEndpointForAdulteryCoResp() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(loadResourceAsString(INVALID_AOS_OFFLINE_ADULTERY_CO_RESP_JSON_PATH))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    mustBeYesOrNo("CoRespAdmitAdultery")
                ))
            ))
        ));
    }

    private String mandatoryFieldIsMissing(String fieldName) {
        return String.format("Mandatory field \"%s\" is missing", fieldName);
    }

    private String notAValidDate(String fieldName) {
        return String.format("%s must be a valid 8 digit date", fieldName);
    }

    private String mustBeYesOrNo(String fieldName) {
        return String.format("%s must be \"Yes\" or \"No\"", fieldName);
    }
}