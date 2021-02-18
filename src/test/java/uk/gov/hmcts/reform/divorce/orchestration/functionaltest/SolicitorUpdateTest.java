package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class SolicitorUpdateTest extends IdamTestSupport {

    private static final String API_URL = "/solicitor-update";
    private static final String DRAFT_MINI_PETITION_TEMPLATE_NAME = "divorcedraftminipetition";
    private static final String SOL_APPLICANT_APPLICATION_SUBMITTED_TEMPLATE_ID = "93c79e53-e638-42a6-8584-7d19604e7697";

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockEmailClient;

    private FeatureToggleService featureToggleService;

    // update all test methods' names
    // add feature toggles and see how it woudl work for feature toggle ON/OFF

    @Test
    public void givenCaseData_whenSolicitorUpdate_thenCallsAll() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDgsCall(ccdCallbackRequest);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(mockEmailClient).sendEmail(
            eq(SOL_APPLICANT_APPLICATION_SUBMITTED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString()
        );
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_AndRepresentedRespondentJourneyIsOff_thenReturnWithMappedOrgPolicyReferences() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(mvcResult.getResponse().getContentAsString(),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE)),
                hasJsonPath("$.data.RespondentOrganisationPolicy.OrgPolicyReference", is(TEST_RESPONDENT_SOLICITOR_REFERENCE)))
        );
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_AndNotRepresented_thenReturnWithUnMappedRespondentOrgPolicyReference() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, TEST_RESPONDENT_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(mvcResult.getResponse().getContentAsString(),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.respondentSolicitorReference"),
                assertPetitionerOrganisationPolicyFieldIsPopulated(),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy"))
        );
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_AndNoSolicitorReferencesThenReturnWithNoOrganisationPolicyReferences() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDgsCall(ccdCallbackRequest);

        MvcResult mvcResult = callCallbackEndpointSuccessfully(ccdCallbackRequest);

        assertThat(mvcResult.getResponse().getContentAsString(),
            allOf(
                hasNoJsonPath("$.data.D8SolicitorReference"),
                hasNoJsonPath("$.data.PetitionerOrganisationPolicy"),
                hasNoJsonPath("$.data.respondentSolicitorReference"),
                hasNoJsonPath("$.data.RespondentOrganisationPolicy")
            )
        );
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

        CaseDetails fullCase = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(expectedData)
            .build();

        return CcdCallbackRequest.builder()
            .eventId(CREATE_EVENT)
            .caseDetails(fullCase)
            .build();
    }

    private OrganisationPolicy buildOrganisationPolicy() {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .build();
    }

    private Matcher<? super Object> assertPetitionerOrganisationPolicyFieldIsPopulated() {
        return hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE));
    }
}
