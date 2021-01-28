package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Collections;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class SolicitorDnFetchDocTest extends IntegrationTest {

    private static final String AWAITING_DN_CASE = "fixtures/solicitor/solicitor-awaiting-dn-data.json";
    private static final String AWAITING_DN_ALTERNATIVE_SERVICE_CASE = "fixtures/solicitor/solicitor-awaiting-dn-alternative-data.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenDraftDN_ThenReturnWithRespondentAnswersDocumentLink() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(AWAITING_DN_CASE, CcdCallbackRequest.class);
        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getData(),
            isJson(
                allOf(
                    withJsonPath("$.data.respondentanswerslink"),
                    withJsonPath("$.errors", nullValue()))
            ));
    }

    @Test
    public void givenRespondentAnswersInDraftDN_ThenReturnWithError() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(AWAITING_DN_CASE, CcdCallbackRequest.class);
        ccdCallbackRequest.getCaseDetails().getCaseData().put("D8DocumentsGenerated", Collections.emptyList());

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getData(),
            isJson(
                allOf(
                    withJsonPath("$.data.respondentanswerslink"),
                    withJsonPath("$.errors", hasSize(1)))
            ));
    }

    @Test
    public void givenDraftDN_AlternativeService_ThenReturnWithoutRespondentAnswersDocumentLink() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(AWAITING_DN_ALTERNATIVE_SERVICE_CASE, CcdCallbackRequest.class);
        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getData(),
            isJson(
                allOf(
                    hasNoJsonPath("$.data.respondentanswerslink"),
                    withJsonPath("$.errors", nullValue()))
            ));
    }
}
