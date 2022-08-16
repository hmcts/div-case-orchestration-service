package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.cases;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.map.SingletonMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_TITLE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_PARTIES_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COST_ORDER_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.DN_APPROVAL_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.FAMILY_MAN_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.COURT_HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CTSC_CONTACT_DETAILS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CASE_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

public class RemoveListingTest extends IdamTestSupport {

    private static final String API_URL = "/bulk/remove/listing";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequest.json";
    private static final String BULK_CASE_ID = "1505150515051550";
    private static final String CASE_ID_FIRST = "1558711395612316";
    private static final String CASE_ID_SECOND = "1558711407435839";

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCallbackRequestWithTwoCaseLinks_thenTriggerBulkCaseUpdateEvent() throws Exception {
        stubSignInForCaseworker();

        stubDocumentGeneratorService(CASE_LIST_FOR_PRONOUNCEMENT.getTemplateByLanguage(ENGLISH),
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CaseDetails.builder()
                .caseId(BULK_CASE_ID)
                .state("someState")
                .caseData(buildExpectedCaseData())
                .build()),
            CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(loadResourceAsString(REQUEST_JSON_PATH))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        waitAsyncCompleted();
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

    private Map<String, Object> buildExpectedCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CTSC_CONTACT_DETAILS_KEY, getCtscContactDetails());
        caseData.put(COURT_NAME, "Name of Court");
        caseData.put(COURT_HEARING_DATE, "2000-01-01T10:20:55.000");
        caseData.put(BULK_CASE_TITLE_KEY, "Title");
        caseData.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, "District Judge");
        caseData.put(CASE_LIST_KEY, asList(
            buildCaseListElement("6c8a94a4-1566-43fa-a797-b559221aaef0", CASE_ID_FIRST),
            buildCaseListElement("72ac194e-8066-49fa-9f54-50b4b0fb79bf", CASE_ID_SECOND)
        ));
        caseData.put(BULK_CASE_ACCEPTED_LIST_KEY, asList(
            buildCaseAcceptedElement("21445968-27b8-4274-bb6d-91d33c27affa", CASE_ID_FIRST),
            buildCaseAcceptedElement("9f9387a0-ae41-4f2c-b6d5-8c8202650e07", CASE_ID_SECOND)
        ));


        return caseData;
    }

    private Map<String, Object> buildCaseListElement(String id, String caseReference) {
        Map<String, Object> elem = new HashMap<>();
        elem.put(ID, id);

        Map<String, Object> value = new HashMap<>();
        value.put(CASE_REFERENCE_FIELD, new SingletonMap(CASE_REFERENCE_FIELD, caseReference));
        value.put(COST_ORDER_FIELD, null);
        value.put(CASE_PARTIES_FIELD, "John Smith vs Jane Jamed");
        value.put(DN_APPROVAL_DATE_FIELD, null);
        value.put(FAMILY_MAN_REFERENCE_FIELD, "LV17D80100");

        elem.put(VALUE_KEY, value);

        return elem;
    }

    private Map<String, Object> buildCaseAcceptedElement(String id, String caseReference) {
        return ImmutableMap.of(
            ID, id,
            VALUE_KEY, new SingletonMap(CASE_REFERENCE_FIELD, caseReference)
        );
    }
}
