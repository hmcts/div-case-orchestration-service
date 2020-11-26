package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice.AosNotReceivedForProcessServerTest.buildCaseDataForPetitioner;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice.AosNotReceivedForProcessServerTest.buildCaseDataForSolicitor;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice.AosNotReceivedForProcessServerTest.getExpectedPetitionerTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice.AosNotReceivedForProcessServerTest.getExpectedSolicitorTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class AlternativeServiceConfirmedTest extends IdamTestSupport {

    private static final String API_URL = "/alternative-service-confirmed";

    private static final String CITIZEN_APPLY_FOR_DN_ALTERNATIVE_SERVICE = "7e13370b-ab0f-47f2-8ff6-99262b37cc1b";
    private static final String PET_SOL_APPLY_FOR_DN_ALTERNATIVE_SERVICE = "3acafb26-695a-4cc1-b7c9-6ee48ed8aab9";

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void petitionerRepresentedThenSendEmailToPetitionerSolicitor() throws Exception {
        callEndpointWithData(buildCaseDataForSolicitor(SERVED_BY_ALTERNATIVE_METHOD));

        verify(emailClient).sendEmail(
            eq(PET_SOL_APPLY_FOR_DN_ALTERNATIVE_SERVICE),
            eq(TEST_SOLICITOR_EMAIL),
            eq(getExpectedSolicitorTemplateVars()),
            any()
        );
    }

    @Test
    public void petitionerNotRepresentedThenSendEmailToPetitioner() throws Exception {
        callEndpointWithData(buildCaseDataForPetitioner(SERVED_BY_ALTERNATIVE_METHOD));

        verify(emailClient).sendEmail(
            eq(CITIZEN_APPLY_FOR_DN_ALTERNATIVE_SERVICE),
            eq(TEST_PETITIONER_EMAIL),
            eq(getExpectedPetitionerTemplateVars()),
            any()
        );
    }

    @Test
    public void servedByProcessFieldIsNotYesThenDontSendAnyEmail() throws Exception {
        callEndpointWithData(new HashMap<>());

        verifyNoInteractions(emailClient);
    }

    private void callEndpointWithData(Map<String, Object> caseData) throws Exception {
        CcdCallbackRequest input = new CcdCallbackRequest(
            AUTH_TOKEN,
            "confirmAlternativeService",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }
}
