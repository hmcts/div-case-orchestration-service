package uk.gov.hmcts.reform.divorce.callback;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CASE_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class EditBulkCaseListingTest extends IntegrationTest {

    private static final String TEMPLATE_ID = CASE_LIST_FOR_PRONOUNCEMENT.getTemplateByLanguage(ENGLISH);

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void whenEditBulkListingWithJudge_thenReturnUpdatedBulkData() {
        String futureDateTime = LocalDateTime.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE_TIME);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(ImmutableMap.of(
                COURT_HEARING_DATE_CCD_FIELD, futureDateTime,
                PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_PRONOUNCEMENT_JUDGE
            )).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        Map<String, Object> response = cosApiClient.editBulkListing(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest,
            TEMPLATE_ID, CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE, CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME);
        String jsonResponse = objectToJson(response);

        assertThat(
            jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME + TEST_CASE_ID))
        );
    }
}
