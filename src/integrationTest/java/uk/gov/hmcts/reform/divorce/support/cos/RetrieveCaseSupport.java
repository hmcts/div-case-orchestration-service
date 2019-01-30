package uk.gov.hmcts.reform.divorce.support.cos;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class RetrieveCaseSupport extends CcdSubmissionSupport {

    protected static final String RETRIEVED_DATA_COURT_ID_KEY = "data.courts";

    @Value("${case.orchestration.retrieve-case.context-path}")
    private String retrieveCaseContextPath;

    protected Response retrieveCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.getFromRestService(
                serverUrl + retrieveCaseContextPath,
                headers,
                null
        );
    }

}