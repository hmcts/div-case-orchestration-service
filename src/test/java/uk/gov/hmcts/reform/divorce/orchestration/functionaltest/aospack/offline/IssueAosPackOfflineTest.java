package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class IssueAosPackOfflineTest extends MockedFunctionalTest {

    private static final String API_URL = "/issue-aos-pack-offline/parties/%s";

    private static final byte[] FIRST_FILE_BYTES = "firstFile".getBytes();
    private static final byte[] SECOND_FILE_BYTES = "secondFile".getBytes();
    private static final String EXPECTED_ENCODED_FILES_CONTENT = new JSONArray()
        .put(Base64.getEncoder().encodeToString(FIRST_FILE_BYTES))
        .put(Base64.getEncoder().encodeToString(SECOND_FILE_BYTES))
        .toString();

    private static final String RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00075.doc";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00076.doc";
    private static final String AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID = "FL-DIV-APP-ENG-00080.docx";
    private static final String AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID = "FL-DIV-APP-ENG-00084.docx";

    private static final String TEST_PAYLOAD = "/jsonExamples/payloads/genericPetitionerDataWithPreviouslyGeneratedDocument.json";
    private static final String TEST_DOCUMENT_ID_IN_EXAMPLE_PAYLOAD = "1234";

    @Value("${bulk-print.dueDate}")
    private Integer dueDateOffset;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        resetAllMockServices();

        stubSendLetterService(OK);
        stubServiceAuthProvider(OK, TEST_SERVICE_AUTH_TOKEN);
        stubDMStore(TEST_DOCUMENT_ID_IN_EXAMPLE_PAYLOAD, "existingDocumentContent".getBytes());
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", SEPARATION_TWO_YEARS.getValue());

        String invitationLetterDocumentId = stubDocumentGeneratorService(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE
        );
        String invitationLetterFilename = RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        String formDocumentId = stubDocumentGeneratorService(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE
        );
        String formFilename = AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME + caseDetails.getCaseId();

        stubDMStore(invitationLetterDocumentId, FIRST_FILE_BYTES);
        stubDMStore(formDocumentId, SECOND_FILE_BYTES);

        webClient.perform(post(format(API_URL, RESPONDENT.getDescription()))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.RespContactMethodIsDigital", is(NO_VALUE)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(3)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasItems(
                    hasJsonPath("value.DocumentFileName", is(invitationLetterFilename)),
                    hasJsonPath("value.DocumentFileName", is(formFilename))
                ))
            )));

        //Verifying interactions
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(containing(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)));
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(containing(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID)));
        sendLetterService.verify(postRequestedFor(urlEqualTo("/letters")).withRequestBody(
            matchingJsonPath("$.documents", equalToJson(EXPECTED_ENCODED_FILES_CONTENT, false, false))
        ).withRequestBody(
            matchingJsonPath("$.additional_data.letterType", equalTo("aos-pack-offline-respondent"))
        ));
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForCoRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", ADULTERY.getValue());

        String invitationLetterDocumentId = stubDocumentGeneratorService(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE
        );
        String invitationLetterFilename = CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        String formDocumentId = stubDocumentGeneratorService(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE
        );
        String formFilename = AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME  + caseDetails.getCaseId();

        stubDMStore(invitationLetterDocumentId, FIRST_FILE_BYTES);
        stubDMStore(formDocumentId, SECOND_FILE_BYTES);

        webClient.perform(post(format(API_URL, CO_RESPONDENT.getDescription()))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.CoRespContactMethodIsDigital", is(NO_VALUE)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(3)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasItems(
                    hasJsonPath("value.DocumentFileName", is(invitationLetterFilename)),
                    hasJsonPath("value.DocumentFileName", is(formFilename))
                ))
            )));

        //Verifying interactions
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(containing(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)));
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(containing(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID)));
        sendLetterService.verify(postRequestedFor(urlEqualTo("/letters")).withRequestBody(
            matchingJsonPath("$.documents", equalToJson(EXPECTED_ENCODED_FILES_CONTENT, false, false))
        ).withRequestBody(
            matchingJsonPath("$.additional_data.letterType", equalTo("aos-pack-offline-co-respondent"))
        ));
    }

    @Test
    public void testEndpointReturnsErrorMessages_WhenDivorcePartyIsInvalid() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);

        webClient.perform(post(format(API_URL, "invalid-divorce-party"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isBadRequest());

        documentGeneratorServiceServer.verify(0, postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH)));
    }

    @Test
    public void testEndpointReturnsExpectedDueDateInResponse_ForRespondent() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", SEPARATION_TWO_YEARS.getValue());

        String invitationLetterDocumentId = stubDocumentGeneratorService(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE
        );

        String formDocumentId = stubDocumentGeneratorService(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE
        );

        stubDMStore(invitationLetterDocumentId, FIRST_FILE_BYTES);
        stubDMStore(formDocumentId, SECOND_FILE_BYTES);

        String expectedDueDate = DateCalculator.getDateWithOffset(dueDateOffset);

        webClient.perform(post(format(API_URL, RESPONDENT.getDescription()))
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.dueDate", is(expectedDueDate)))));
    }

    @Test
    public void testEndpointReturnsNoDueDateInResponse_ForCoRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", ADULTERY.getValue());

        String invitationLetterDocumentId = stubDocumentGeneratorService(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE
        );

        String formDocumentId = stubDocumentGeneratorService(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails),
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE
        );

        stubDMStore(invitationLetterDocumentId, FIRST_FILE_BYTES);
        stubDMStore(formDocumentId, SECOND_FILE_BYTES);

        webClient.perform(post(format(API_URL, CO_RESPONDENT.getDescription()))
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson())))
            .andExpect(content().string(hasNoJsonPath("$.data.dueDate")));
    }
}
