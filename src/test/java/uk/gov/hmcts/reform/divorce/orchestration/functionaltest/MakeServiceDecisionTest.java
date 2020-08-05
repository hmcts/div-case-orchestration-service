package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.OrderToDispense;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;

import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateFromLocalDate;

public class MakeServiceDecisionTest extends IdamTestSupport {

    private static final String API_URL = "/make-service-decision";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Test
    public void shouldPopulateReceivedServiceAddedDateInResponse() throws Exception {
        CcdCallbackRequest input = buildRequest();

        Map<String, Object> expectedCaseData = ImmutableMap.of(
            SERVICE_APPLICATION_DECISION_DATE,
            formatDateFromLocalDate(now())
        );

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(convertObjectToJsonString(expectedCaseData))));
    }

    @Test
    public void shouldGenerateOderInDispenseAndAddItToResponse() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest input = buildRequest(caseData);
        CcdCallbackResponse expectedResponse = buildExpectedResponse(caseData);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    private Map<String, Object> buildInputCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, "2010-10-10");
        caseData.put(SERVICE_APPLICATION_TYPE, DISPENSED);
        return caseData;
    }

    private CcdCallbackResponse buildExpectedResponse(Map<String, Object> caseData) {
        Map<String, Object> caseDataBeforeGeneratingPdf = new HashMap<>(caseData);
        caseDataBeforeGeneratingPdf.put(SERVICE_APPLICATION_DECISION_DATE, formatDateFromLocalDate(now()));

        String orderToDispenseDocumentId = stubDocumentGeneratorServiceBaseOnContextPath(
            OrderToDispenseGenerationTask.FileMetadata.TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", TEST_CASE_ID,
                    "case_data", buildOrderToDispense(caseDataBeforeGeneratingPdf)
                )
            ),
            OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE
        );

        Map<String, Object> expectedCaseData = new HashMap<>(caseDataBeforeGeneratingPdf);

        expectedCaseData.put(D8DOCUMENTS_GENERATED, singletonList(
            createCollectionMemberDocumentAsMap(
                getDocumentStoreTestUrl(orderToDispenseDocumentId),
                OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE,
                null)
        ));

        return CcdCallbackResponse.builder()
            .state(CcdStates.SERVICE_APPLICATION_NOT_APPROVED)
            .data(expectedCaseData)
            .build();
    }

    private OrderToDispense buildOrderToDispense(Map<String, Object> caseData) {
        return OrderToDispense.orderToDispenseBuilder()
            .caseReference(TEST_CASE_ID)
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .documentIssuedOn(DatesDataExtractor.getLetterDate())
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationDecisionDate(DatesDataExtractor.getServiceApplicationDecisionDate(caseData))
            .build();
    }

    private CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "event",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }

    private CcdCallbackRequest buildRequest() {
        return buildRequest(new HashMap<>());
    }
}
