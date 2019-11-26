package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.NEW_DIVORCE_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ValidationBulkScanITest {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String WARNINGS_STATUS = "WARNINGS";
    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/fullD8Form.json";
    private static final String INCOMPLETE_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/validation/incompleteForm.json";
    private static final String VALIDATE_OCR_ENDPOINT = "/forms/{form-type}/validate-ocr";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessResponseForValidationEndpoint() throws Exception {
        String formToValidate = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        mockMvc.perform(post(VALIDATE_OCR_ENDPOINT, NEW_DIVORCE_CASE)
            .contentType(APPLICATION_JSON)
            .content(formToValidate)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", equalTo(emptyList())),
                hasJsonPath("$.errors", equalTo(emptyList())),
                hasJsonPath("$.status", equalTo(SUCCESS_STATUS))
            ))
        ));
    }

    @Test
    public void shouldReturnErrorResponseForValidationEndpoint() throws Exception {
        String formToValidate = loadResourceAsString(INCOMPLETE_D8_FORM_JSON_PATH);

        mockMvc.perform(post(VALIDATE_OCR_ENDPOINT, NEW_DIVORCE_CASE)
            .contentType(APPLICATION_JSON)
            .content(formToValidate)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItem("Mandatory field \"D8PetitionerFirstName\" is missing")),
                hasJsonPath("$.errors", equalTo(emptyList())),
                hasJsonPath("$.status", equalTo(WARNINGS_STATUS))
            ))
        ));
    }

    @Test
    public void shouldReturnResourceNotFoundResponseForUnsupportedFormType() throws Exception {
        String formToValidate = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        mockMvc.perform(post(VALIDATE_OCR_ENDPOINT, "unsupportedFormType")
            .contentType(APPLICATION_JSON)
            .content(formToValidate)
        ).andExpect(matchAll(
            status().isNotFound(),
            content().string(isEmptyString())
        ));
    }
}