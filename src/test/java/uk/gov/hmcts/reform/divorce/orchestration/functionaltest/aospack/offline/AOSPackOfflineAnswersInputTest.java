package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class AOSPackOfflineAnswersInputTest {

    private static final String CCD_RESPONSE_DATA_FIELD = "data";

    @Autowired
    private MockMvc mockMvc;

    private CcdCallbackRequest ccdCallbackRequest;

    @Before
    public void setUp() throws Exception {
        ccdCallbackRequest = getJsonFromResourceFile("/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFields_ForRespondent_ForAdultery() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_AOS_ADMIT_ADULTERY, YES_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", "respondent")
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(notNullValue())),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_ADMIT_OR_CONSENT_TO_FACT, is(YES_VALUE))
                ))
            )));
    }

    @Test
    public void shouldReturnOnlyAutomaticFields_ForCoRespondent_ForAdultery() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", CO_RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasNoJsonPath(STATE_CCD_FIELD),
                    hasNoJsonPath(RECEIVED_AOS_FROM_RESP)
                ))
            )));
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFieldsIfDefended_ForUnreasonableBehaviour() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(AOS_SUBMITTED_AWAITING_ANSWER)),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_WILL_DEFEND_DIVORCE, is(YES_VALUE))
                ))
            )));
    }

    @Test
    public void shouldReturnNewStateAndAutomaticFieldsIfUndefended_ForUnreasonableBehaviour() throws Exception {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        mockMvc.perform(post("/processAosOfflineAnswers/parties/{party}", RESPONDENT.getDescription())
            .contentType(APPLICATION_JSON)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(STATE_CCD_FIELD, is(AWAITING_DECREE_NISI)),
                    hasJsonPath(RECEIVED_AOS_FROM_RESP, is(YES_VALUE)),
                    hasJsonPath(RESP_WILL_DEFEND_DIVORCE, is(NO_VALUE))
                ))
            )));
    }
}