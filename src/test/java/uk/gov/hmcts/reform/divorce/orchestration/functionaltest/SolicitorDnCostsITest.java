package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;


import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SolicitorDnCostsITest {

    private static final String API_URL = "/sol-dn-costs";
    private static final String DIVORCE_COSTS_OPTION_DN_ENUM = "DivorceCostsOptionDNEnum";

    @Autowired
    private MockMvc webClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseData_whenSolicitorIsOnDnCostsPage_thenSetCosts() throws Exception {

        final Map<String, Object> caseData = ObjectMapperTestUtil.getJsonFromResourceFile(
                "/jsonExamples/payloads/sol-dn-costs-resp-agree.json",
                Map.class
        );

        final Map<String, Object> expectedOptions = ObjectMapperTestUtil.getJsonFromResourceFile(
                "/jsonExamples/payloads/sol-dn-costs-resp-agree-expected-options.json",
                Map.class
        );

        final CaseDetails caseDetails =
                CaseDetails.builder()
                        .caseData(caseData)
                        .build();

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(DIVORCE_COSTS_OPTION_DN_ENUM, expectedOptions);
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(expectedData)
                .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));

    }
}
