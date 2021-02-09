package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class IssueAosPackOfflineTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenIssuingAosPackOfflineForRespondent_thenReturnCallbackResponseWithRightDocuments() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String testCaseId = caseDetails.getCaseId();
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());

        Map<String, Object> response = cosApiClient.issueAosPackOffline(createCaseWorkerUser().getAuthToken(),
            RESPONDENT.getDescription(),
            ccdCallbackRequest);
        String jsonResponse = objectToJson(response);

        assertThat(
            jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated", allOf(
                hasSize(2),
                hasJsonPath("[0].value.DocumentFileName",
                        is(RESPONDENT_AOS_INVITATION_LETTER_FILENAME + testCaseId)),
                hasJsonPath("[1].value.DocumentFileName",
                        is(AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME + testCaseId))
            )));
    }

    @Test
    public void givenCase_whenIssuingAosPackOfflineForCoRespondent_thenReturnCallbackResponseWithRightDocuments() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String testCaseId = caseDetails.getCaseId();
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());

        Map<String, Object> response = cosApiClient.issueAosPackOffline(createCaseWorkerUser().getAuthToken(),
            DivorceParty.CO_RESPONDENT.getDescription(),
            ccdCallbackRequest);
        String jsonResponse = objectToJson(response);

        assertThat(
            jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated", allOf(
                hasSize(2),
                hasJsonPath("[0].value.DocumentFileName",
                        is(CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME + testCaseId)),
                hasJsonPath("[1].value.DocumentFileName",
                        is(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME + testCaseId))
            )));
    }

}