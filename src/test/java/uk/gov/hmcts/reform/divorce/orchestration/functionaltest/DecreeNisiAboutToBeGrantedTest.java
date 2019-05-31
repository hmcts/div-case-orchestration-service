package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPROVAL_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DecreeNisiAboutToBeGrantedTest {

    private static final String API_URL = "/dn-about-to-be-granted";
    private static final String CCD_RESPONSE_DATA_FIELD = "data";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CcdUtil ccdUtil;

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDNGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            )
        ));

        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT)),
                    hasJsonPath(DN_APPROVAL_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDN_NOTGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            )
        ));

        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasNoJsonPath(DN_APPROVAL_DATE_FIELD)
                ))
            )));
    }

}