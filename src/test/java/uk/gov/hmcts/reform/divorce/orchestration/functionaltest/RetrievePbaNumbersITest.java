package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.OrganisationEntityResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.PBAOrganisationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class RetrievePbaNumbersITest extends IdamTestSupport {

    private static final String API_URL = "/retrieve-pba-numbers";
    private static final String RETRIEVE_PBA_NUMBERS_URL = "/refdata/external/v1/organisations/pbas?email=testRespondentSolicitor%40email.com";

    @Autowired
    private MockMvc webClient;

    @MockBean
    protected AuthUtil authUtil;

    private Map<String, Object> caseData;
    private CaseDetails caseDetails;
    private CcdCallbackRequest ccdCallbackRequest;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
    }

    @Test
    public void givenSolicitorEmail_whenRetrievePbaNumbers_andPbaNumbersFound_thenReturn_CaseWithPbaNumbers() throws Exception {

        List<String> pbaNumbersOfSolicitor = ImmutableList.of("pbaNumber1", "pbaNumber2");
        caseData.put(PBA_NUMBERS, asDynamicList(pbaNumbersOfSolicitor));

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        whenRetrievePbaNumbersExpect(expectedResponse, pbaNumbersOfSolicitor, caseData);
    }

    @Test
    public void givenSolicitorEmail_whenRetrievePbaNumbers_andNoPbaNumbersFound_thenReturn_CasewithNoPbaNumbers() throws Exception {
        List<String> pbaNumbersOfSolicitor = Collections.emptyList();

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(asList("No PBA number found for this account, please contact your organisation."))
            .build();

        whenRetrievePbaNumbersExpect(expectedResponse, pbaNumbersOfSolicitor, caseData);
    }

    @Test
    public void givenSolicitorEmailAndSolPaymentMethodNotPba_whenRetrievePbaNumbers_thenReturn_CaseGiven() throws Exception {
        List<String> pbaNumbersOfSolicitor = Collections.emptyList();
        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        whenRetrievePbaNumbersExpect(expectedResponse, pbaNumbersOfSolicitor, caseData);
    }

    private void whenRetrievePbaNumbersExpect(CcdCallbackResponse expectedResponse, List<String> pbaNumbersOfSolicitor,
                                              Map<String, Object> caseData) throws Exception {
        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        stubRetrievePbaNumbersEndpoint(HttpStatus.OK, buildPbaResponse(pbaNumbersOfSolicitor));
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubUserDetailsEndpoint(HttpStatus.OK, BEARER_AUTH_TOKEN, getUserDetailsResponse());

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }


    private void stubRetrievePbaNumbersEndpoint(HttpStatus status, PBAOrganisationResponse response) {
        pbaValidationServer.stubFor(WireMock.get(urlEqualTo(RETRIEVE_PBA_NUMBERS_URL))
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION_HEADER, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private PBAOrganisationResponse buildPbaResponse(List<String> pbaNumbers) {
        return PBAOrganisationResponse.builder()
            .organisationEntityResponse(
                OrganisationEntityResponse.builder()
                    .paymentAccount(pbaNumbers)
                    .build())
            .build();
    }

    private String getUserDetailsResponse() {
        return ObjectMapperTestUtil.convertObjectToJsonString(
            UserDetails.builder()
                .email(TEST_RESP_SOLICITOR_EMAIL)
                .build());
    }
}
