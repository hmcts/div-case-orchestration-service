package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SolicitorAmendPetitionForRefusalITest extends MockedFunctionalTest {
    private static final String CASE_ID = "1234567890";
    private static final String API_URL = "/solicitor-amend-petition-dn-rejection";
    private static final String PREVIOUS_ID = "Test.Id";
    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String SOLICITOR_SUBMISSION_CONTEXT_PATH = "/casemaintenance/version/1/solicitor-submit";
    private static final String CMS_AMEND_PETITION_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/amended-petition-draft-refusal/%s",
        CASE_ID
    );

    private static final ValidationResponse validationResponseOk = ValidationResponse.builder().build();
    private static final CaseDetails caseDetails =
        CaseDetails.builder().caseId(CASE_ID)
        .caseData(Collections.emptyMap())
        .build();
    private static final CcdCallbackRequest ccdCallbackRequest =
        CcdCallbackRequest.builder()
        .caseDetails(caseDetails).build();

    @Autowired
    private MockMvc webClient;

    @MockBean
    private ValidationService validationService;

    @Test
    public void givenJWTTokenIsNull_whenSolicitorAmendPetitionForRefusal_thenReturnBadRequest()
        throws Exception {
        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenCreateNewCaseAndUpdateOldCase()
        throws Exception {

        Map<String, Object> draftData = new HashMap<>();
        draftData.put(PREVIOUS_CASE_ID_JSON_KEY, PREVIOUS_ID);
        Map<String, Object> formattedDraftData = new HashMap<>();
        formattedDraftData.put(D_8_CASE_REFERENCE, TEST_CASE_ID);

        String content = convertObjectToJsonString(draftData);
        String formattedContent = convertObjectToJsonString(formattedDraftData);

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.OK, content);
        stubFormatterServerEndpoint(HttpStatus.OK, formattedContent);
        stubMaintenanceServerEndpointForSubmit(HttpStatus.OK, formattedContent);
        when(validationService.validate(any())).thenReturn(validationResponseOk);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private void stubCmsAmendPetitionDraftEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.put(CMS_AMEND_PETITION_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void stubFormatterServerEndpoint(HttpStatus status, String body) {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void stubMaintenanceServerEndpointForSubmit(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.post(SOLICITOR_SUBMISSION_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }
}

