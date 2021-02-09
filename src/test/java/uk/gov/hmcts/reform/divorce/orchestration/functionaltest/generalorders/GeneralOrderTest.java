package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalorders;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.DivorceGeneralOrder;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.GeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralOrderDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.JudgeTypesLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.PETITIONER;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_PARTIES;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_JUDGE_TYPE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralOrderTest extends IdamTestSupport {

    private static final String API_URL = "/create-general-order/final";

    @Autowired
    protected MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Autowired
    private JudgeTypesLookupService judgeTypesLookupService;

    @Before
    public void setup() {
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldGenerateOrderTheFirstGeneralOrderAndCreateCollection() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest input = buildRequest(caseData);
        String documentType = GeneralOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;
        String fileName = formatDocumentFileName(documentType);

        stubDocumentGeneratorServiceRequest(caseData, GeneralOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                getAllJsonKeysThatShouldNotExistWhenSuccess(),
                hasJsonPath("$.data.GeneralOrders", hasSize(1)),
                hasJsonPath("$.data.GeneralOrders[0].value.Document.DocumentType", is(documentType)),
                hasJsonPath("$.data.GeneralOrders[0].value.Document.DocumentLink.document_filename",
                    is(fileName)),
                hasJsonPath("$.data.GeneralOrders[0].value.GeneralOrderParties[0]",
                    is(PETITIONER.getDescription()))
            )));
    }

    @Test
    public void shouldGenerateOrderAnotherGeneralOrderAndAddItToExistingCollection() throws Exception {
        Map<String, Object> caseData = addGeneralOrderCollection(buildInputCaseData());
        CcdCallbackRequest input = buildRequest(caseData);
        String documentType = GeneralOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;
        String fileName = formatDocumentFileName(documentType);

        stubDocumentGeneratorServiceRequest(caseData, GeneralOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                getAllJsonKeysThatShouldNotExistWhenSuccess(),
                hasJsonPath("$.data.GeneralOrders", hasSize(2)),
                hasJsonPath("$.data.GeneralOrders[1].value.Document.DocumentType", is(documentType)),
                hasJsonPath("$.data.GeneralOrders[1].value.Document.DocumentLink.document_filename",
                    is(fileName)),
                hasJsonPath("$.data.GeneralOrders[1].value.GeneralOrderParties[0]",
                    is(PETITIONER.getDescription()))
            )));
    }

    @Test
    public void shouldHandleGeneralOrderServiceException() throws Exception {
        CcdCallbackRequest input = buildRequest(emptyMap());

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", hasSize(1)),
                hasJsonPath("$.errors[0]", containsString("Could not evaluate value of mandatory property \"JudgeName\"")),
                hasNoJsonPath("$.warnings")
            )));
    }

    private Map<String, Object> addGeneralOrderCollection(Map<String, Object> caseData) {
        CollectionMember<DivorceGeneralOrder> member = new CollectionMember<>();
        member.setValue(
            DivorceGeneralOrder.builder()
                .document(new Document())
                .generalOrderParties(asList(PETITIONER, RESPONDENT))
                .build()
        );

        caseData.put(GENERAL_ORDERS, asList(member));

        return caseData;
    }

    protected Map<String, Object> buildInputCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);

        caseData.put(CO_RESPONDENT_LINKED_TO_CASE, YES_VALUE);
        caseData.put(JUDGE_TYPE, TEST_JUDGE_TYPE_CODE);
        caseData.put(JUDGE_NAME, TEST_JUDGE_NAME);
        caseData.put(GENERAL_ORDER_DETAILS, TEST_GENERAL_ORDER_DETAILS);
        caseData.put(GENERAL_ORDER_DATE, TEST_GENERAL_ORDER_DATE);
        caseData.put(GENERAL_ORDER_RECITALS, TEST_GENERAL_ORDER_RECITALS);
        caseData.put(GENERAL_ORDER_PARTIES, asList(PETITIONER.getDescription()));

        caseData.put(GENERAL_ORDER_DRAFT, new HashMap<>());

        return caseData;
    }

    protected void stubDocumentGeneratorServiceRequest(Map<String, Object> caseData, String templateId, String documentType) {
        Map<String, Object> caseDataBeforeGeneratingPdf = new HashMap<>(caseData);

        GeneralOrder generalOrder = buildPopulatedTemplateModel(caseDataBeforeGeneratingPdf);

        stubDocumentGeneratorService(
            templateId,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", generalOrder.getCaseReference(),
                    "case_data", generalOrder
                )
            ),
            documentType
        );
    }

    protected GeneralOrder buildPopulatedTemplateModel(Map<String, Object> caseData) {
        return GeneralOrder.generalOrderBuilder()
            .caseReference(TEST_CASE_ID)
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .hasCoRespondent(PartyRepresentationChecker.isCoRespondentLinkedToCase(caseData))
            .coRespondentFullName(FullNamesDataExtractor.getCoRespondentFullName(caseData))
            .judgeName(GeneralOrderDataExtractor.getJudgeName(caseData))
            .judgeType(getJudgeTypeByCode(caseData))
            .generalOrderRecitals(GeneralOrderDataExtractor.getGeneralOrderRecitals(caseData))
            .generalOrderDetails(GeneralOrderDataExtractor.getGeneralOrderDetails(caseData))
            .generalOrderDate(GeneralOrderDataExtractor.getGeneralOrderDate(caseData))
            .build();
    }

    private String getJudgeTypeByCode(Map<String, Object> caseData) {
        try {
            return judgeTypesLookupService.getJudgeTypeByCode(GeneralOrderDataExtractor.getJudgeType(caseData));
        } catch (JudgeTypeNotFoundException e) {
            fail(e.getMessage());
        }

        return null;
    }

    protected CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "createGeneralOrder",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }

    protected String formatDocumentFileName(String documentType) {
        return documentType + DateUtils.formatDateTimeForDocument(LocalDateTime.now()) + ".pdf";
    }

    private Matcher<String> getAllJsonKeysThatShouldNotExistWhenSuccess() {
        return allOf(
            hasNoJsonPath("$.data.GeneralOrderRecitals"),
            hasNoJsonPath("$.data.GeneralOrderParties"),
            hasNoJsonPath("$.data.GeneralOrderDate"),
            hasNoJsonPath("$.data.GeneralOrderDetails"),
            hasNoJsonPath("$.data.JudgeType"),
            hasNoJsonPath("$.data.JudgeName"),
            hasNoJsonPath("$.errors"),
            hasNoJsonPath("$.warnings")
        );
    }
}
