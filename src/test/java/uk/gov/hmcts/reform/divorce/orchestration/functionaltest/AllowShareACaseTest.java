package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;

public class AllowShareACaseTest extends IdamTestSupport {

    private static final String API_URL_CREATE = "/allow-share-a-case";

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldCallAllApis() throws Exception {
        final CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, USER_DETAILS_PIN_USER_JSON);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubRemoveCaseRoleServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN);
        stubAssignCaseAccessServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        webClient.perform(post(API_URL_CREATE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private String getBody(CcdCallbackRequest ccdCallbackRequest) {
        return ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest);
    }

    private CcdCallbackRequest buildRequest() {
        return CcdCallbackRequest.builder()
            .eventId(CREATE_EVENT)
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .caseData(emptyMap())
                    .build()
            ).build();
    }
}
