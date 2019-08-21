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
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class EditBulkCaseListingTest extends IntegrationTest {

    private static final String DOCUMENT_TYPE = "caseListForPronouncement";
    private static final String TEMPLATE_ID = "FL-DIV-GNO-ENG-00059.docx";
    private static final String FILE_NAME = "caseListForPronouncement";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenEditBulkListingWithJudge_thenReturnUpdatedBulkData() {

        String futureDateTime = LocalDateTime.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE_TIME);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(ImmutableMap.of(
                COURT_HEARING_DATE_CCD_FIELD, futureDateTime,
                PRONOUNCEMENT_JUDGE_CCD_FIELD, "District Judge"
            )).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        Map<String, Object> response = cosApiClient.editBulkListing(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest,
            TEMPLATE_ID, DOCUMENT_TYPE, FILE_NAME);
        String jsonResponse = objectToJson(response);

        assertThat(
            jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(FILE_NAME + TEST_CASE_ID))
        );
    }

}
