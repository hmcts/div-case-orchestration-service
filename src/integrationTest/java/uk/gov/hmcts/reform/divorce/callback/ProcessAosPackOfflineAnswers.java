package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class ProcessAosPackOfflineAnswers extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenIssuingAosPackOfflineForRespondent_thenReturnCallbackResponseWithRightDocuments() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS);

        CcdCallbackResponse response = cosApiClient.processAosPackOfflineAnswers(createCaseWorkerUser().getAuthToken(),
            RESPONDENT.getDescription(),
            ccdCallbackRequest);
        String jsonResponse = objectToJson(response);

        assertThat(jsonResponse, hasJsonPath(DATA, allOf(
            hasJsonPath(D8DOCUMENTS_GENERATED, hasSize(1)),
            hasJsonPath(D8DOCUMENTS_GENERATED, hasItem(
                hasJsonPath("value", hasJsonPath(DOCUMENT_TYPE_JSON_KEY, is(DOCUMENT_TYPE_RESPONDENT_ANSWERS)))
            ))
        )));
    }

}