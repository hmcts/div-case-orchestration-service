package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.RetryRule;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;

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
import static uk.gov.hmcts.reform.divorce.callback.SolicitorCreateAndUpdateTest.postWithDataAndValidateResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

public class ProcessPbaPaymentTest extends IntegrationTest {

    private static final String DELETED_ACCOUNT = "PBA0078600";
    private static final String MISSING_DATA_ACCOUNT = "PBA0082848";
    private static final String NON_EXISTING_ACCOUNT = "PBA1357924";

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";
    private static final String PAYMENT_FIXTURES = "payment/";

    private static final String AWAITING_PAYMENT_CONFIRMATION = CcdStates.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION;
    private static final String SUBMITTED = CcdStates.SUBMITTED;

    // One attempt only needed as any other attempt within 2min would still fail
    @Rule
    public RetryRule retryRule = new RetryRule(1);

    @Value("${case.orchestration.solicitor.process-pba-payment.context-path}")
    private String contextPath;

    @Test
    public void givenValidRequestWithFeeAccountThenReturnStateAsSubmitted() throws Exception {
        Response cosResponse = getOkResponseWithRequestData("active-account-request");

        assertThat(getResponseBody(cosResponse),
            allOf(
                isResponseWithExpectedState(SUBMITTED),
                hasJsonPath("$.data.D8DocumentsGenerated[*]..DocumentType", not(hasItem(DOCUMENT_TYPE_PETITION)))
            ));
    }

    @Test
    public void givenValidRequestWithHelpWithFeeThenReturnStateAsSolicitorAwaitingPaymentConfirmation() throws Exception {
        Response cosResponse = getOkResponseWithRequestData("help-with-fee-request");

        assertThat(getResponseBody(cosResponse), isResponseWithExpectedState(AWAITING_PAYMENT_CONFIRMATION));
    }

    @Test
    public void givenValidRequestWithInactiveAccountThenHandle403AndReturnWithCAE0004ErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData("inactive-account-request");
        String expectedErrorMessage = getExpectedErrorMessage(DELETED_ACCOUNT, PbaErrorMessage.CAE0004);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    @Test
    public void givenValidRequestWithNonExistingAccountThenHandle404AndReturnWithErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData("nonexisting-account-request");
        String expectedErrorMessage = getExpectedErrorMessage(NON_EXISTING_ACCOUNT, PbaErrorMessage.NOTFOUND);

        assertThat(getResponseBody(cosResponse), isResponseWithErrorMessage(expectedErrorMessage));
    }

    @Test
    public void givenValidRequestWithInvalidOrMissingDataThenHandle422AndReturnWithErrorMessage() throws Exception {
        Response cosResponse = getOkResponseWithRequestData("missing-data-request");
        String expectedErrorMessage = getExpectedErrorMessage(MISSING_DATA_ACCOUNT, PbaErrorMessage.GENERAL);

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
        return postWithDataAndValidateResponse(
            serverUrl + contextPath,
            PAYLOAD_CONTEXT_PATH + PAYMENT_FIXTURES + fixtureFileName + ".json",
            createCaseWorkerUser().getAuthToken()
        );
    }
}
