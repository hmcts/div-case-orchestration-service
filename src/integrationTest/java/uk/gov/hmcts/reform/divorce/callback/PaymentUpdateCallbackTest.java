package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PaymentUpdateCallbackTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/callback/";
    private static final String SUBMIT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/update/";

    @Value("${case.orchestration.payment-update.context-path}")
    private String contextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Test
    public void givenValidPaymentRequest_whenPaymentUpdate_thenReturnStatusOkWithNoErrors() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, createCitizenUser().getAuthToken());

        UserDetails citizenUser = createCitizenUser();

        String caseId = ccdClientSupport.submitCase(
                ResourceLoader.loadJsonToObject(SUBMIT_PAYLOAD_CONTEXT_PATH + "submit-case-data.json", Map.class),
                citizenUser
        ).getId().toString();

        PaymentUpdate paymentUpdate = ResourceLoader.loadJsonToObject(
                PAYLOAD_CONTEXT_PATH + "paymentUpdate.json", PaymentUpdate.class
        );

        paymentUpdate.setCcdCaseNumber(caseId);

        Response response = RestUtil.putToRestService(
                serverUrl + contextPath,
                Collections.singletonMap(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()),
                ResourceLoader.objectToJson(paymentUpdate)
        );

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

}
