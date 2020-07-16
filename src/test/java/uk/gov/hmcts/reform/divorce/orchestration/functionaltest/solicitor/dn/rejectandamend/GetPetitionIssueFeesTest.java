package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.solicitor.dn.rejectandamend;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GetPetitionIssueFeesTest extends GetPetitionIssueFeesAbstractTest {

    @Test
    public void givenCaseData_whenAddingCaseRoleFails_thenReturnErrorResponse() throws Exception {
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.BAD_GATEWAY);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem setting the [PETSOLICITOR] role to the case"))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenUnauthorizedRequest_whenGetPetitionIssueFees_thenReturnErrorData() throws Exception {
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.FORBIDDEN);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem setting the [PETSOLICITOR] role to the case"))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

}