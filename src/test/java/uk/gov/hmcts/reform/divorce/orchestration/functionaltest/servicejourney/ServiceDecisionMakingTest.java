package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.buildInputCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.buildRefusalRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.buildServiceRefusalOrderCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.generateDocumentLink;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ServiceDecisionMakingTest extends IdamTestSupport {

    private static final String API_URL = "/service-decision-made/draft";

    private CtscContactDetails ctscContactDetails;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Before
    public void setup() {
        ctscContactDetails = ctscContactDetailsDataProviderService.getCtscContactDetails();
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldGenerateDraftDeemedServiceRefusalOrderWhenInReview() throws Exception {
        String templateId = DeemedServiceRefusalOrderDraftTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderDraftTask.FileMetadata.DOCUMENT_TYPE;
        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForServiceRefusalDraft());
    }

    @Test
    public void shouldGenerateDraftDispensedServiceRefusalOrderWhenInReview() throws Exception {
        String templateId = DispensedServiceRefusalOrderDraftTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderDraftTask.FileMetadata.DOCUMENT_TYPE;

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForServiceRefusalDraft());
    }

    @Test
    public void shouldNotGenerateAnyRefusalOrderDraftsWhenServiceApplicationIsGrantedAndSubmitted() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(YES_VALUE, ApplicationServiceTypes.DISPENSED);
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.data.D8DocumentsGenerated"),
                hasNoJsonPath("$.data.ServiceRefusalDraft"),
                hasNoJsonPath("$.errors")
            )));
    }

    private ResultMatcher commonExpectationsForServiceRefusalDraft() {
        return content().string(allOf(
            isJson(),
            hasJsonPath("$.data.ServiceApplicationDocuments", hasSize(0)),
            hasJsonPath("$.data.ServiceRefusalDraft"),
            hasJsonPath("$.data.ServiceRefusalDraft.document_url", notNullValue()),
            hasJsonPath("$.data.ServiceRefusalDraft.document_filename", notNullValue()),
            hasJsonPath("$.data.ServiceRefusalDraft.document_binary_url", notNullValue()),
            hasNoJsonPath("$.errors")
        ));
    }

    private ServiceApplicationRefusalOrder buildPopulatedServiceRefusalOrderTemplateModel(Map<String, Object> caseData) {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .ctscContactDetails(ctscContactDetails)
            .petitionerFullName(getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .caseReference((String) caseData.get(CASE_ID_JSON_KEY))
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationRefusalReason(getServiceApplicationRefusalReason(caseData))
            .documentIssuedOn(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .build();
    }

    private CcdCallbackRequest buildServiceRefusalOrderFixture(
        String templateId, String serviceType, String documentType) {
        DocumentLink refusalDraftDocument = generateDocumentLink(templateId);
        Map<String, Object> refusalOrderData = buildServiceRefusalOrderCaseData(
            serviceType, refusalDraftDocument
        );

        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(refusalOrderData);
        ccdCallbackRequest.getCaseDetails().setState(AWAITING_SERVICE_CONSIDERATION);

        stubDocumentGeneratorService(
            templateId,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", refusalOrderData.get(CASE_ID_JSON_KEY),
                    "case_data", buildPopulatedServiceRefusalOrderTemplateModel(refusalOrderData)
                )),
            documentType
        );
        return ccdCallbackRequest;
    }
}
