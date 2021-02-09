package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

public class SolicitorCreateTest extends IdamTestSupport {

    private static final String API_URL_CREATE = "/solicitor-create";
    private static final String DRAFT_MINI_PETITION_TEMPLATE_NAME = "divorcedraftminipetition";

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnServiceCentreCourtAllocation() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDraftDocumentGeneratorService(
            DRAFT_MINI_PETITION_TEMPLATE_NAME,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            AddMiniPetitionDraftTask.DOCUMENT_TYPE
        );

        webClient.perform(post(API_URL_CREATE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnWithMappedOrgPolicyReference() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .build());

        stubDraftDocumentGeneratorService(
            DRAFT_MINI_PETITION_TEMPLATE_NAME,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            AddMiniPetitionDraftTask.DOCUMENT_TYPE
        );

        MvcResult mvcResult = webClient.perform(post(API_URL_CREATE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString(),
            allOf(
                hasJsonPath("$.data.D8SolicitorReference"),
                hasJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(TEST_SOLICITOR_REFERENCE)))
        );
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnWithNoOrganisationPolicyReferenceSet() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildRequest();

        stubDraftDocumentGeneratorService(
            DRAFT_MINI_PETITION_TEMPLATE_NAME,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            AddMiniPetitionDraftTask.DOCUMENT_TYPE
        );

        MvcResult mvcResult = webClient.perform(post(API_URL_CREATE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(getBody(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString(),
            allOf(
                hasNoJsonPath("$.data.D8SolicitorReference"),
                hasNoJsonPath("$.data.PetitionerOrganisationPolicy")
            )
        );
    }

    private String getBody(CcdCallbackRequest ccdCallbackRequest) {
        return ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest);
    }

    private CcdCallbackRequest buildRequest() {
        Map<String, Object> expectedData = new HashMap<>();
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
}
