package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.PrepareToPrintForPronouncementTest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.UpdateBulkCaseHearingDetailsTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class DocumentGeneratedTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case.json";
    private static final String TEST_CASE_ID = "0123456789012345";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";

    private CcdCallbackRequest ccdCallbackRequest;

    @Autowired
    private CosApiClient cosApiClient;

    @Before
    public void setUp() {
        ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
    }

    @Test
    public void shouldGenerateAndReturnDocumentForMiniPetition() {

        Map<String, Object> response = cosApiClient
            .generateDocument(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest,
                MINI_PETITION_TEMPLATE_NAME, DOCUMENT_TYPE_PETITION, MINI_PETITION_TEMPLATE_NAME);

        assertGeneratedDocumentMatchesFileName(response, MINI_PETITION_TEMPLATE_NAME);
    }

    @Test
    public void shouldGenerateAndReturnDocumentWhenPreparingToPrintForPronouncement() {
        Map<String, Object> response = cosApiClient
            .prepareToPrintForPronouncement(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);

        assertGeneratedDocumentMatchesFileName(response, PrepareToPrintForPronouncementTest.EXPECTED_FILE_NAME_PREFIX);
    }

    @Test
    public void shouldGenerateAndReturnDocumentWhenUpdatingBulkCaseHearingDetails() {
        Map<String, Object> response = cosApiClient
            .updateBulkCaseHearingDetails(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);

        assertGeneratedDocumentMatchesFileName(response, UpdateBulkCaseHearingDetailsTest.EXPECTED_FILE_NAME_PREFIX);
    }

    private void assertGeneratedDocumentMatchesFileName(Map<String, Object> response, String fileNamePrefix) {
        String jsonResponse = objectToJson(response);
        assertThat(jsonResponse, hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName",
            is(fileNamePrefix + TEST_CASE_ID)
        ));
    }

}