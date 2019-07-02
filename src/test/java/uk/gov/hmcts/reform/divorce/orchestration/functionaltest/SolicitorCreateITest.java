package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SolicitorCreateITest {
    private static final String API_URL_CREATE = "/solicitor-create";
    private static final String API_URL_CREATED = "/solicitor-created";
    private static final String ADD_PETITIONER_SOLICITOR_ROLE = String
        .format("/casemaintenance/version/1/add-petitioner-solicitor-role/%s", TEST_CASE_ID);

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

    private static final CcdCallbackRequest CREATE_EVENT = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnEastMidlandsCourtAllocation() throws Exception {
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(CREATED_DATE_JSON_KEY, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        expectedData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedData)
            .build();

        webClient.perform(post(API_URL_CREATE)
            .content(convertObjectToJsonString(CREATE_EVENT))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenCaseData_whenSolicitorCreatedCallback_thenReturnCallbackResponse() throws Exception {
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(CASE_DATA)
            .build();

        webClient.perform(post(API_URL_CREATED)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CREATE_EVENT))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubMaintenanceServerEndpointForAddPetitionerSolicitorRole() {
        maintenanceServiceServer.stubFor(put(ADD_PETITIONER_SOLICITOR_ROLE)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }
}
