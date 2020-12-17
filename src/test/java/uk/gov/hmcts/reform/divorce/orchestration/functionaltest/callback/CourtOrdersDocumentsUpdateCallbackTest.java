package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class CourtOrdersDocumentsUpdateCallbackTest extends MockedFunctionalTest {

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws Exception {
        Map<String, Object> caseData = Map.of();

        String response = webClient.perform(
            post("/updateCourt")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(
                    CcdCallbackRequest.builder()
                        .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build())
                        .build()
                ))
        )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(response, is(notNullValue()));
//        ObjectMapperTestUtil.getObjectMapperInstance().readT//TODO- transform into CcdCallbackResponse
        //TODO - assert return
    }

}