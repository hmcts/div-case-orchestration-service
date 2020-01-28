package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class HealthCheckITest extends MockedFunctionalTest {

    private static final String HEALTH_UP_RESPONSE = "{ \"status\": \"UP\"}";
    private static final String HEALTH_DOWN_RESPONSE = "{ \"status\": \"DOWN\"}";

    @LocalServerPort
    private int port;

    private String healthUrl;
    private final HttpClient httpClient = HttpClients.createMinimal();

    private HttpResponse getHealth() throws Exception {
        final HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + port + "/health";
    }

    @Test
    public void givenAllDependenciesAreUp_whenCheckHealth_thenReturnStatusUp() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));

        assertThat(JsonPath.read(body, "$.details.featureToggleServiceHealthCheck.status").toString(), equalTo("UP"));

        assertThat(JsonPath.read(body, "$.details.sendLetterServiceHealthCheck.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenFeatureToggleServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, false);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, false);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.featureToggleServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenSendLetterServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, false);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, false);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.featureToggleServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.sendLetterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCaseFormatterServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, false);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenDocumentGeneratorServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, false);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCaseMaintenanceServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, false);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenFeesAndPaymentsServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, false);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenPaymentServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, false);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenServiceAuthIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, true);
        mockEndpointAndResponse(maintenanceServiceServer, true);
        mockEndpointAndResponse(validationServiceServer, true);
        mockEndpointAndResponse(documentGeneratorServiceServer, true);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, true);
        mockEndpointAndResponse(idamServer, true);
        mockEndpointAndResponse(paymentServiceServer, true);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenAllDependenciesAreDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(formatterServiceServer, false);
        mockEndpointAndResponse(maintenanceServiceServer, false);
        mockEndpointAndResponse(validationServiceServer, false);
        mockEndpointAndResponse(documentGeneratorServiceServer, false);
        mockEndpointAndResponse(featureToggleService, true);
        mockEndpointAndResponse(feesAndPaymentsServer, false);
        mockEndpointAndResponse(idamServer, false);
        mockEndpointAndResponse(paymentServiceServer, false);
        mockEndpointAndResponse(sendLetterService, true);
        mockEndpointAndResponse(serviceAuthProviderServer, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseMaintenanceServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.feesAndPaymentsServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.paymentServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    private void mockEndpointAndResponse(WireMockClassRule mockServer, boolean serviceUp) {
        mockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(serviceUp ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)));
    }
}
