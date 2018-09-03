package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AuthenticateRespondentTest extends IntegrationTest {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Value("${case.orchestration.authenticate-respondent.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenAuthenticateUser_thenReturnBadRequest() {
        Response cosResponse = authenticateUser(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenAuthenticateUser_thenReturnForbidden() {
        Response cosResponse = authenticateUser(INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenUserDoNotHaveLetterHolderRole_whenAuthenticateUser_thenReturnUnAuthorized() {
        Response cosResponse = authenticateUser(createCitizenUser().getAuthToken());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenUserHasLetterHolderRole_whenAuthenticateUser_thenReturnOk() {
        Response cosResponse = authenticateUser(createCitizenUser("letter-133307").getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
    }

    private Response authenticateUser(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return
            RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                null
            );
    }
}
