package uk.gov.hmcts.reform.divorce.bulk.scan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
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


    private static final String FULL_D_8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/fullD8Form.json";
    private static final String AOS_OFFLINE_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/aos/2yearSeparation/aosOffline2yrSep.json";
    private static String VALIDATION_END_POINT = "/forms/{form-type}/validate-ocr";
    private static String TRANSFORMATION_END_POINT = "/transform-exception-record";
    private static String UPDATE_END_POINT = "/update-case";
    private String validBody;

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        validBody = loadResourceAsString(FULL_D_8_FORM_JSON_PATH);

        Response validationResponse = validationEndpointRequest(token,VALIDATION_END_POINT, D8_FORM);

        assertEquals(HttpStatus.OK.value(), validationResponse.getStatusCode());
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        validBody = loadResourceAsString(FULL_D_8_FORM_JSON_PATH);

        Response validationResponse = validationEndpointRequest(token,VALIDATION_END_POINT, D8_FORM);

        assertEquals(HttpStatus.FORBIDDEN.value(), validationResponse.getStatusCode());
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        validBody = loadResourceAsString(FULL_D_8_FORM_JSON_PATH);

        Response transformationResponse = transformationAndUpdateEndpointRequest(token,TRANSFORMATION_END_POINT);

        assertEquals(HttpStatus.OK.value(), transformationResponse.getStatusCode());
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        validBody = loadResourceAsString(FULL_D_8_FORM_JSON_PATH);

        Response transformationResponse = transformationAndUpdateEndpointRequest(token,TRANSFORMATION_END_POINT);

        assertEquals(HttpStatus.FORBIDDEN.value(), transformationResponse.getStatusCode());
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        validBody = loadResourceAsString(AOS_OFFLINE_FORM_JSON_PATH);

        Response updateResponse = transformationAndUpdateEndpointRequest(token, UPDATE_END_POINT);

        assertEquals(HttpStatus.OK.value(), updateResponse.getStatusCode());
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        validBody = loadResourceAsString(AOS_OFFLINE_FORM_JSON_PATH);

        Response updateResponse = transformationAndUpdateEndpointRequest(token, UPDATE_END_POINT);

        assertEquals(HttpStatus.FORBIDDEN.value(), updateResponse.getStatusCode());
    }

    private Response validationEndpointRequest(String token, String endpointName, String formType) {

        return SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(validBody)
            .post(cosBaseURL + endpointName, formType);
    }

    private Response transformationAndUpdateEndpointRequest(String token, String endpointName) {

        return SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(validBody)
            .post(cosBaseURL + endpointName);
    }
}