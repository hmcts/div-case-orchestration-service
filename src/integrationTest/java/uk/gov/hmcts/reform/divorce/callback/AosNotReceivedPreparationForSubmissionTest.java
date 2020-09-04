package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE;

public class AosNotReceivedPreparationForSubmissionTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/callback/basic-case.json";

    @Test
    public void givenCase_whenSubmitAOSNotReceived_thenReturnAOSOverdueCoverLetter() throws Exception {
        RestAssured
            .given()
            .header(AUTHORIZATION, createCaseWorkerUser().getAuthToken())
            .contentType(ContentType.JSON)
            .body(ResourceLoader.loadJson(BASE_CASE_RESPONSE))
            .when()
            .post(serverUrl + "/prepare-aos-not-received-for-submission")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("errors", is(nullValue()))
            .body("data.D8DocumentsGenerated", hasItem(hasJsonPath("value.DocumentType", is(AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE))))
        ;
    }

}