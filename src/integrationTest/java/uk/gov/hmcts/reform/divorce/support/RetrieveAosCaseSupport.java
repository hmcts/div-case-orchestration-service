package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RetrieveAosCaseSupport extends CcdSubmissionSupport {
    protected static final String CASE_ID_KEY = "caseId";

    @Value("${case.orchestration.maintenance.retrieve-aos-case.context-path}")
    private String contextPath;

    protected Response retrieveAosCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.getFromRestService(
            serverUrl + contextPath,
            headers,
            Collections.singletonMap("checkCcd", true)
        );
    }

}
