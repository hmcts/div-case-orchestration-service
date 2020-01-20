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

import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;


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

    private static String VALID_BODY;
    private static String VALIDATION_END_POINT = "/forms/{form-type}/validate-ocr";
    private static String TRANSFORMATION_END_POINT = "/transform-exception-record";
    private static String UPDATE_END_POINT = "/update-case";
    private final String fullD8FormJsonPath = "jsonExamples/payloads/bulk/scan/d8/fullD8Form.json";
    private final String aosOfflineFormJsonPath = "jsonExamples/payloads/bulk/scan/aos/aosPackOfflineForm.json";

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        VALID_BODY = loadResourceAsString(fullD8FormJsonPath);

        Response forValidationEndpoint = responseForValidationEndpoint(token,VALIDATION_END_POINT, D8_FORM);

        assert forValidationEndpoint.getStatusCode() == 200 : "Service is not authorised to OCR validation "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        VALID_BODY = loadResourceAsString(fullD8FormJsonPath);

        Response forValidationEndpoint = responseForValidationEndpoint(token,VALIDATION_END_POINT, D8_FORM);

        assert forValidationEndpoint.getStatusCode() == 403 : "Not matching with expected Error code "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        VALID_BODY = loadResourceAsString(fullD8FormJsonPath);

        Response forTransformationEndpoint = responseForTransformationAndUpdateEndpoint(token,TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        VALID_BODY = loadResourceAsString(fullD8FormJsonPath);

        Response forTransformationEndpoint = responseForTransformationAndUpdateEndpoint(token,TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        VALID_BODY = loadResourceAsString(aosOfflineFormJsonPath);

        Response forUpdateEndpoint = responseForTransformationAndUpdateEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case "
            + forUpdateEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        VALID_BODY = loadResourceAsString(aosOfflineFormJsonPath);

        Response forUpdateEndpoint = responseForTransformationAndUpdateEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
            + forUpdateEndpoint.getStatusCode();
    }

    private Response responseForValidationEndpoint(String token, String endpointName, String formType) {

        Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseURL + endpointName, formType);
        return response;
    }

    private Response responseForTransformationAndUpdateEndpoint(String token, String endpointName) {

        Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseURL + endpointName);
        return response;
    }
}