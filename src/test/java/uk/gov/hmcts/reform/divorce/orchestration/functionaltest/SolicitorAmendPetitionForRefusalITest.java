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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMENDED_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_REFERENCE_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SolicitorAmendPetitionForRefusalITest extends MockedFunctionalTest {

    private static final String CASE_ID = "1234567890";
    private static final String NEW_CASE_ID = "1234";
    private static final String API_URL = "/solicitor-amend-petition-dn-rejection";
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
        draftData.put(PREVIOUS_CASE_ID_JSON_KEY, CASE_ID);
        Map<String, Object> formattedDraftData = new HashMap<>();
        formattedDraftData.put(ID, NEW_CASE_ID);

        String content = convertObjectToJsonString(draftData);
        stubCmsAmendPetitionDraftEndpoint(HttpStatus.OK, content);

        String formattedContent = convertObjectToJsonString(formattedDraftData);
        stubMaintenanceServerEndpointForSubmit(HttpStatus.OK, formattedContent);

        when(validationService.validate(any(), anyString())).thenReturn(validationResponseOk);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                hasJsonPath("data" ,
                    hasJsonPath(AMENDED_CASE_ID_CCD_KEY,
                        hasJsonPath(CASE_REFERENCE_CCD_KEY, is(NEW_CASE_ID)))
                )));
    }

    private void stubCmsAmendPetitionDraftEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.put(CMS_AMEND_PETITION_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    private void stubMaintenanceServerEndpointForSubmit(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.post(SOLICITOR_SUBMISSION_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }
}

