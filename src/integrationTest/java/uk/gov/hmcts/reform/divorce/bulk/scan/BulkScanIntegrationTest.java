package uk.gov.hmcts.reform.divorce.bulk.scan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.NEW_DIVORCE_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;

@Slf4j
public class BulkScanIntegrationTest extends IntegrationTest {

    @Autowired
    private IdamUtils idamUtilsS2SAuthorization;

    @Value("${auth.provider.bulkscan.validate.microservice}")
    private String bulkScanValidationMicroService;

    @Value("${auth.provider.bulkscan.update.microservice}")
    private String bulkScanTransformationAndUpdateMicroService;

    @Value("${case.orchestration.service.base.uri}")
    private String cosBaseURL;


    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/fullD8Form.json";
    private static String VALID_BODY;

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        Response forValidationEndpoint = responseForValidationEndpoint(token,"/forms/{form-type}/validate-ocr", NEW_DIVORCE_CASE);


        assert forValidationEndpoint.getStatusCode() == 200 : "Service is not authorised to OCR validation " + forValidationEndpoint.getStatusCode();

    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        Response forValidationEndpoint = responseForValidationEndpoint(token,"/forms/{form-type}/validate-ocr", NEW_DIVORCE_CASE);

        assert forValidationEndpoint.getStatusCode() == 403 : "Not matching with expected Error code " + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
        Response forTransformationEndpoint = responseForEndpoint(token,"/transform-exception-record");

        assert forTransformationEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case" + forTransformationEndpoint.getStatusCode();

    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        Response forTransformationEndpoint = responseForEndpoint(token,"/transform-exception-record");

        assert forTransformationEndpoint.getStatusCode() == 403 : "Not matching with expected error Code " + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
        Response forUpdateEndpoint = responseForEndpoint(token,"/update-case");

        assert forUpdateEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case" + forUpdateEndpoint.getStatusCode();

    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        Response forUpdateEndpoint = responseForEndpoint(token,"/update-case");

        assert forUpdateEndpoint.getStatusCode() == 403 : "Not matching with expected error Code " + forUpdateEndpoint.getStatusCode();
    }

    private Response responseForEndpoint(String token, String endpointName){

           Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseURL+ endpointName);
           return response;
    }
    private Response responseForValidationEndpoint(String token, String endpointName, String formType){

        Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseURL+ endpointName, formType);
        return response;
    }
}