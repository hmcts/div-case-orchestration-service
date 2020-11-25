package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
public class SolicitorPersonalServiceAosOverdueTest extends MockedFunctionalTest {

    private static final String API_URL = "/migrate-to-personal-service-pack";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoSolicitorServiceMethod_thenResponseContainsErrors() throws Exception {

        final Map<String, Object> caseData = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .state(AOS_OVERDUE)
                .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(Collections.emptyMap())),
                        hasJsonPath("$.errors",
                                hasItem("Failed to migrate case to personal service - "
                                        + "Could not evaluate value of mandatory property \"SolServiceMethod\"")
                        )
                )));
    }

    @Test
    public void givenServiceMethodIsNotCourtService_thenResponseContainsErrors() throws Exception {

        final Map<String, Object> caseData = new HashMap<>();

        caseData.put(SOL_SERVICE_METHOD_CCD_FIELD, "TestValue");

        final CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(AOS_OVERDUE)
                .caseData(caseData)
                .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(Collections.emptyMap())),
                        hasJsonPath("$.errors",
                                hasItem("Failed to migrate case to personal service - This event can only be used"
                                        + " with for a case with Court Service as the service method")
                        )
                )));
    }

    @Test
    public void givenServiceMethodIsCourtService_thenResponse() throws Exception {

        final Map<String, Object> caseData = new HashMap<>();

        caseData.put(SOL_SERVICE_METHOD_CCD_FIELD, COURT_SERVICE_VALUE);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(AOS_OVERDUE)
            .caseData(caseData)
            .build();

        final Map<String, Object> migratedCaseData = new HashMap<>();

        migratedCaseData.put(SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE);

        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        maintenanceServiceServer.stubFor(WireMock.request(POST.name(),urlEqualTo("/casemaintenance/version/1/updateCase/test.case.id/"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{}")));

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data",
                    is(is(Collections.emptyMap()))),
                hasJsonPath("$.errors",
                    is(emptyOrNullString())
                )
            )));
    }
}
