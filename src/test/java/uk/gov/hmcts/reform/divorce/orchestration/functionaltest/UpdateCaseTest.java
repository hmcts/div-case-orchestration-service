//package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;
//
//import com.github.tomakehurst.wiremock.client.WireMock;
//import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
//import com.github.tomakehurst.wiremock.matching.EqualToPattern;
//import org.junit.ClassRule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//
//import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
//import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
//import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
//
//import java.util.*;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
//import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
//import static org.springframework.http.HttpHeaders.AUTHORIZATION;
//import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
//import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import static org.hamcrest.CoreMatchers.containsString;
//
//import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
//
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = OrchestrationServiceApplication.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@PropertySource(value = "classpath:application.yml")
//@AutoConfigureMockMvc
//public class UpdateCaseTest {
//
//    private static final String CASE_ID = "01234567890";
//    private static final String EVENT_ID = "updateEvent";
//    private static final String AUTH_TOKEN = "authToken";
//
//    private static final String API_URL = String.format("/updateCase/%s/%s", CASE_ID, EVENT_ID);
//
//    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
//    private static final String UPDATE_CONTEXT_PATH = String.format(
//        "/casemaintenance/version/1/updateCase/%s/$s",
//        CASE_ID,
//        EVENT_ID
//    );
//
//    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();
//
//    @Autowired
//    private MockMvc webClient;
//
//    @ClassRule
//    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);
//
//    @ClassRule
//    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);
//
//    @Test
//    public void givenCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnSuccess() throws Exception {
//        Map<String, Object> responseData = new HashMap<>();
//        responseData.put("Hello", "World");
//
//        stubFormatterServerEndpoint(CASE_DATA, CASE_DATA);
//        stubMaintenanceServerEndpointForUpdate(CASE_DATA, responseData);
//
//        webClient.perform(post(API_URL)
//                .header(AUTHORIZATION, AUTH_TOKEN)
//                .content(convertObjectToJsonString(CASE_DATA))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().string(containsString(responseData.toString())));
//    }
//
//    @Test
//    public void givenNoPayload_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
//        webClient.perform(post(API_URL)
//                .header(AUTHORIZATION, AUTH_TOKEN)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void givenNoAuthToken_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
//        webClient.perform(post(API_URL)
//                .content(convertObjectToJsonString(CASE_DATA))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//
//    private void stubFormatterServerEndpoint(Map<String, Object> transformToCCDFormat, Map<String, Object> response)
//            throws Exception {
//        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
//                .withRequestBody(equalToJson(convertObjectToJsonString(transformToCCDFormat)))
//                .willReturn(aResponse()
//                        .withStatus(HttpStatus.OK.value())
//                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
//                        .withBody(convertObjectToJsonString(response))));
//    }
//
//    private void stubMaintenanceServerEndpointForUpdate(Map<String, Object> caseData, Map<String, Object> response)
//            throws Exception {
//        maintenanceServiceServer.stubFor(WireMock.post(UPDATE_CONTEXT_PATH)
//                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
//                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
//                .willReturn(aResponse()
//                        .withStatus(HttpStatus.OK.value())
//                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
//                        .withBody(convertObjectToJsonString(response))));
//    }
//}
