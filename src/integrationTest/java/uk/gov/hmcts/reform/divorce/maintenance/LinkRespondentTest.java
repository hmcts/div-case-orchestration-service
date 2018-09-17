package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LinkRespondentTest extends RetrieveAosCaseSupport {

    @Value("${case.orchestration.maintenance.link-respondent.context-path}")
    private String contextPath;

    @Test
    public void givenValidCaseDetails_whenLinkRespondent_thenCaseShouldBeLinked() {
        final String pinUserFirstName = "pinuserfirstname";
        final String pinUserLastName = "pinuserfirstname";

        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(pinUserFirstName, pinUserLastName, petitionerUserDetails.getAuthToken());

        final CaseDetails caseDetails = submitCase(
            "submit-complete-case.json",
            createCaseWorkerUser(),
            ImmutablePair.of("AosLetterHolderId", pinResponse.getUserId())
        );

        updateCase(String.valueOf(caseDetails.getId()), null, "testAosAwaiting");

        final UserDetails respondentUserDetails = createCitizenUser();

        Response linkResponse =
            linkRespondent(
                respondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(respondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));
    }

    private Response linkRespondent(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
            serverUrl + contextPath + "/" + caseId + "/" + pin,
            headers,
            null
        );
    }
}
