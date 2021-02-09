package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DaAboutToBeGrantedTest extends MockedFunctionalTest {

    private static final String API_URL = "/da-about-to-be-granted";
    private static final String DECREE_ABSOLUTE_TEMPLATE_ID = "FL-DIV-GOR-ENG-00062.docx";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_PRONOUNCEMENT_JUDGE)
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE)
        .build();

    private static final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
        .caseDetails(CaseDetails.builder().caseData(CASE_DATA).caseId(TEST_CASE_ID).build())
        .build();

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailService mockEmailService;

    @MockBean
    private Clock clock;

    @Before
    public void setUp() {
        LocalDateTime grantedDate = LocalDateTime.parse(TEST_DECREE_ABSOLUTE_GRANTED_DATE);
        when(clock.instant()).thenReturn(grantedDate.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
        when(clock.withZone(DateUtils.Settings.ZONE_ID)).thenReturn(clock);
    }

    @Test
    public void givenCorrectRespondentDetails_ThenOkResponse() throws Exception {
        stubDocumentGeneratorService(DECREE_ABSOLUTE_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ObjectMapperTestUtil.convertObject(ccdCallbackRequest.getCaseDetails(), Map.class)),
            DECREE_ABSOLUTE_DOCUMENT_TYPE);
        when(mockEmailService.sendEmail(anyString(), anyString(), anyMap(), anyString(), any())).thenReturn(null);

        String inputJson = convertObjectToJsonString(ccdCallbackRequest);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(CASE_DATA).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenBodyIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthHeaderIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        String inputJson = convertObjectToJsonString(ccdCallbackRequest);
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(inputJson))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

}