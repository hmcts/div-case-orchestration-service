package uk.gov.hmcts.reform.divorce.callback;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.io.IOException;
import java.util.Collections;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class SolicitorDnFetchDocTest extends IntegrationTest {

    private static final String AWAITING_DN_CASE = "fixtures/solicitor/solicitor-awaiting-dn-data.json";
    private static final String AWAITING_DN_ALTERNATIVE_SERVICE_CASE = "fixtures/solicitor/solicitor-awaiting-dn-alternative-service-data.json";
    private static final String AWAITING_DN_SERVICE_APPLICATION_CASE = "fixtures/solicitor/solicitor-awaiting-dn-service-application-data.json";
    private static final String D8_DOCUMENTS_GENERATED = "D8DocumentsGenerated";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenDraftDN_ThenReturnWithRespondentAnswersDocumentLink() throws IOException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(AWAITING_DN_CASE, CcdCallbackRequest.class);
        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(convertObjectToJsonString(ccdCallbackResponse),
            isJson(
                allOf(
                    withJsonPath("$.data.respondentanswerslink"),
                    withJsonPath("$.errors", nullValue())
                )
            )
        );
    }

    @Test
    public void givenRespondentAnswersInDraftDN_ThenReturnWithError() throws IOException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(AWAITING_DN_CASE, CcdCallbackRequest.class);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_DOCUMENTS_GENERATED, Collections.emptyList());

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(convertObjectToJsonString(ccdCallbackResponse),
            isJson(
                allOf(
                    withoutJsonPath("$.data.respondentanswerslink"),
                    withJsonPath("$.errors", hasSize(1)))
            ));
    }

    @Test
    public void givenDraftDN_AlternativeService_ThenReturnWithoutRespondentAnswersDocumentLink() throws IOException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(AWAITING_DN_ALTERNATIVE_SERVICE_CASE, CcdCallbackRequest.class);
        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(convertObjectToJsonString(ccdCallbackResponse), respondentAnswersNotRequired());
    }

    @Test
    public void givenDraftDN_ServiceApplicationGranted_ThenReturnWithoutRespondentAnswersDocumentLink() throws IOException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(AWAITING_DN_SERVICE_APPLICATION_CASE, CcdCallbackRequest.class);
        CcdCallbackResponse ccdCallbackResponse = cosApiClient.solDnRespAnswersDoc(ccdCallbackRequest);

        assertThat(convertObjectToJsonString(ccdCallbackResponse), respondentAnswersNotRequired());
    }

    private Matcher<Object> respondentAnswersNotRequired() {
        return isJson(
            allOf(
                withoutJsonPath("$.data.respondentanswerslink"),
                withJsonPath("$.errors", nullValue())
            )
        );
    }
}
