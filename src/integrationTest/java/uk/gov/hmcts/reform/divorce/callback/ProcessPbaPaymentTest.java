package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;

import java.util.Random;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class ProcessPbaPaymentTest extends IntegrationTest {

    private static final String ACTIVE_ACCOUNT = "PBA0077051";
    private static final String DELETED_ACCOUNT = "PBA0078600";
    private static final String NON_EXISTING_ACCOUNT = "PBA1357924";

    private static final String PAYLOAD_CONTEXT_PATH = "/fixtures/solicitor/payment/";

    private static final String AWAITING_PAYMENT_CONFIRMATION = CcdStates.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION;
    private static final String SUBMITTED = CcdStates.SUBMITTED;

    private static final Random RANDOM_NUMBER_GENERATOR = new Random();

    @Value("${case.orchestration.solicitor.process-pba-payment.context-path}")
    private String contextPath;

    @Test
    public void givenValidRequestWithFeeAccountThenReturnStateAsSubmitted() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "active-account-request.json");

        assertThat(getResponseBody(cosResponse),
            allOf(
                isResponseWithExpectedState(SUBMITTED),
                hasJsonPath("$.data.D8DocumentsGenerated[*]..DocumentType", not(hasItem(DOCUMENT_TYPE_PETITION)))
            ));
    }

    @Test
    public void givenValidRequestWithHelpWithFeeThenReturnStateAsSolicitorAwaitingPaymentConfirmation() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "help-with-fee-request.json");

        assertThat(getResponseBody(cosResponse), isResponseWithExpectedState(AWAITING_PAYMENT_CONFIRMATION));
    }

    @Test
    public void givenValidRequestWithInsufficientFundsThenHandle403AndReturnWithCAE0001ErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "insufficient-funds-request.json");
        String expectedErrorMessage = getExpectedErrorMessage(ACTIVE_ACCOUNT, PbaErrorMessage.CAE0001);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    @Test
    public void givenValidRequestWithInactiveAccountThenHandle403AndReturnWithCAE0004ErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "inactive-account-request.json");
        String expectedErrorMessage = getExpectedErrorMessage(DELETED_ACCOUNT, PbaErrorMessage.CAE0004);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    @Test
    public void givenValidRequestWithNonExistingAccountThenHandle404AndReturnWithErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "nonexisting-account-request.json");
        String expectedErrorMessage = getExpectedErrorMessage(NON_EXISTING_ACCOUNT, PbaErrorMessage.NOTFOUND);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    @Test
    public void givenValidRequestWithInvalidOrMissingDataThenHandle422AndReturnWithErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData(PAYLOAD_CONTEXT_PATH + "missing-data-request.json");
        String expectedErrorMessage = getExpectedErrorMessage(ACTIVE_ACCOUNT, PbaErrorMessage.GENERAL);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    private Matcher<Object> isResponseWithErrorMessage(String expectedErrorMessage) {
        return isJson(
            allOf(
                withJsonPath("$.errors", hasSize(1)),
                withJsonPath("$.errors[0]", is(expectedErrorMessage)),
                withJsonPath("$.state", nullValue()),
                withJsonPath("$.data", nullValue())
            ));
    }

    private Matcher<Object> isResponseWithExpectedState(String expectedState) {
        return isJson(
            allOf(
                withJsonPath("$.errors", nullValue()),
                withJsonPath("$.state", is(expectedState)),
                withJsonPath("$.data", allOf(
                    notNullValue(),
                    hasNoJsonPath("$.data.PaymentStatus")
                )))
        );
    }

    private String getResponseBody(Response cosResponse) {
        return cosResponse.getBody().asString();
    }

    private String getExpectedErrorMessage(String pbaNumber, PbaErrorMessage pbaErrorMessage) {
        return format(pbaErrorMessage.value(), pbaNumber);
    }

    private Response getOkResponseWithRequestData(String fixtureFileName) throws Exception {
        String randomCaseId = generateRandomCaseId();
        String requestBody = getJsonFromResourceFile(fixtureFileName, JsonNode.class)
            .toString()
            .replace("replace_with_case_id", randomCaseId);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, createCaseWorkerUser().getAuthToken())
            .body(requestBody)
            .when()
            .post(serverUrl + contextPath)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .response();
    }

    private String generateRandomCaseId() {
        int randomCaseId = RANDOM_NUMBER_GENERATOR.nextInt();

        if (randomCaseId < 0) {
            randomCaseId = randomCaseId * -1;
        }

        return String.valueOf(randomCaseId);
    }

}