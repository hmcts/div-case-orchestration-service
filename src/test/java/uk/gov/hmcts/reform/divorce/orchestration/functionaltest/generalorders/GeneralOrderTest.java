package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalorders;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.GeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralOrderDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.AbstractGeneralOrderGenerationTaskTest.TEST_JUDGE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralOrderTest extends IdamTestSupport {

    private static final String API_URL = "/create-general-order/final";

    @Autowired
    protected MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Before
    public void setup() {
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldGenerateOrderToDispenseAndAddItToResponse() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest input = buildRequest(caseData);
        String documentType = GeneralOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;
        String fileName = documentType + DateUtils.formatDateFromLocalDate(LocalDate.now()) + ".pdf";

        stubDgsRequest(caseData, GeneralOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.data.GeneralOrderDraft"),
                hasJsonPath("$.data.GeneralOrders", hasSize(1)),
                hasJsonPath("$.data.GeneralOrders[0].value.DocumentType", is(documentType)),
                hasJsonPath("$.data.GeneralOrders[0].value.DocumentLink.document_filename",
                    is(fileName)),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }

    protected Map<String, Object> buildInputCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);

        caseData.put(CO_RESPONDENT_LINKED_TO_CASE, YES_VALUE);
        caseData.put(JUDGE_TYPE, TEST_JUDGE_TYPE);
        caseData.put(JUDGE_NAME, TEST_JUDGE_NAME);
        caseData.put(GENERAL_ORDER_DETAILS, TEST_GENERAL_ORDER_DETAILS);
        caseData.put(GENERAL_ORDER_DATE, TEST_GENERAL_ORDER_DATE);
        caseData.put(GENERAL_ORDER_RECITALS, TEST_GENERAL_ORDER_RECITALS);

        caseData.put(GENERAL_ORDER_DRAFT, new HashMap<>());

        return caseData;
    }

    protected void stubDgsRequest(Map<String, Object> caseData, String templateId, String documentType) {
        Map<String, Object> caseDataBeforeGeneratingPdf = new HashMap<>(caseData);

        GeneralOrder generalOrder = buildPopulatedTemplateModel(caseDataBeforeGeneratingPdf);

        stubDocumentGeneratorServiceBaseOnContextPath(
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
            .judgeType(GeneralOrderDataExtractor.getJudgeType(caseData))
            .generalOrderRecitals(GeneralOrderDataExtractor.getGeneralOrderRecitals(caseData))
            .generalOrderDetails(GeneralOrderDataExtractor.getGeneralOrderDetails(caseData))
            .generalOrderDate(GeneralOrderDataExtractor.getGeneralOrderDate(caseData))
            .build();
    }

    protected CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "createGeneralOrder",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }
}
