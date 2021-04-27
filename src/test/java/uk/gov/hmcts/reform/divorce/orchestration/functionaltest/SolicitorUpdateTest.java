package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_DIGITAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;

public class SolicitorUpdateTest extends IdamTestSupport {

    private static final String API_URL = "/solicitor-update";
    private static final String DRAFT_MINI_PETITION_TEMPLATE_NAME = "divorcedraftminipetition";

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    public void givenCaseData_whenSolicitorUpdate_thenCallsAll() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDgsCall(ccdCallbackRequest);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubGetMyOrganisationServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        callCallbackEndpointSuccessfully(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOff_thenReturnWithMappedOrgPolicyReferences() throws Exception {
        switchFeatureTogglesOff();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(
            getResponseContent(mvcResult),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", nullValue()),
                hasJsonPath("$.data.RespondentOrganisationPolicy.OrgPolicyReference", nullValue())
            )
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOn_thenReturnWithMappedOrgPolicyReferences() throws Exception {
        switchFeatureTogglesOn();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(
            getResponseContent(mvcResult),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE)),
                hasJsonPath("$.data.RespondentOrganisationPolicy.OrgPolicyReference", is(TEST_RESPONDENT_SOLICITOR_REFERENCE))
            )
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOff_andNotRepresented_thenReturnWithUnMappedRespondentOrgPolicyReference() throws Exception {
        switchFeatureTogglesOff();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(getResponseContent(mvcResult),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", nullValue()),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy")
            )
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOn_andNotRepresented_thenRetWithUnMappedRespOrgPolicyReference() throws Exception {
        switchFeatureTogglesOn();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(RESPONDENT_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME);
        caseData.put(RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESPONDENT_SOLICITOR_PHONE, TEST_SOLICITOR_PHONE);
        caseData.put(RESPONDENT_SOLICITOR_EMAIL, TEST_RESPONDENT_SOLICITOR_EMAIL);
        caseData.put(RESPONDENT_SOLICITOR_ADDRESS, TEST_CO_RESPONDENT_SOLICITOR_ADDRESS);
        caseData.put(RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE);
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);

        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubGetMyOrganisationServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(getResponseContent(mvcResult),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE)),
                hasNoJsonPath("$.data.D8RespondentSolicitorName"),
                hasNoJsonPath("$.data.respondentSolicitorReference"),
                hasNoJsonPath("$.data.D8RespondentSolicitorPhone"),
                hasNoJsonPath("$.data.D8RespondentSolicitorEmail"),
                hasNoJsonPath("$.data.D8DerivedRespondentSolicitorAddr"),
                hasNoJsonPath("$.data.RespSolDigital"),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy")
            )
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOn_andNotDigital_thenRetWithUnMappedRespOrgPolicyReference() throws Exception {
        switchFeatureTogglesOn();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_DIGITAL, NO_VALUE);

        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubGetMyOrganisationServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(getResponseContent(mvcResult),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE)),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy")
            )
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_andRRJourneyIsOff_andNoSolicitorReferences_thenRetWithNoOrgPolicyReferences() throws Exception {
        switchFeatureTogglesOff();

        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(getResponseContent(mvcResult),
            allOf(
                hasNoJsonPath("$.data.D8SolicitorReference"),
                hasNoJsonPath("$.data.PetitionerOrganisationPolicy"),
                hasNoJsonPath("$.data.respondentSolicitorReference"),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy")
            )
        );
    }

    private void switchFeatureTogglesOn() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY))
            .thenReturn(true);
        when(featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE))
            .thenReturn(true);
    }

    private void switchFeatureTogglesOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY))
            .thenReturn(false);
        when(featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE))
            .thenReturn(false);
    }

    private void stubDgsCall(CcdCallbackRequest ccdCallbackRequest) {
        stubDraftDocumentGeneratorService(
            DRAFT_MINI_PETITION_TEMPLATE_NAME,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            AddMiniPetitionDraftTask.DOCUMENT_TYPE
        );
    }

    private MvcResult callCallbackEndpointSuccessfully(CcdCallbackRequest ccdCallbackRequest) throws Exception {
        return webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    private String getBody(CcdCallbackRequest ccdCallbackRequest) {
        return ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest);
    }

    private CcdCallbackRequest buildRequest() {
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        expectedData.put(CREATED_DATE_JSON_KEY, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.SERVICE_CENTER.getId());
        expectedData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.SERVICE_CENTER.getSiteId());
        expectedData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        expectedData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        expectedData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        expectedData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        expectedData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedData.put(NEW_LEGAL_CONNECTION_POLICY_CCD_DATA, YES_VALUE);

        CaseDetails fullCase = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(expectedData)
            .build();

        return CcdCallbackRequest.builder()
            .eventId(CREATE_EVENT)
            .caseDetails(fullCase)
            .build();
    }

    private String getResponseContent(MvcResult mvcResult) throws UnsupportedEncodingException {
        return mvcResult.getResponse().getContentAsString();
    }
}
