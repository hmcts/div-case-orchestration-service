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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TransformationBulkScanITest {

    private static final String PARTIAL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/partialD8Form.json";
    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/fullD8Form.json";
    private static final String TRANSFORMATION_URL = "/transform-exception-record";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingFullDataSet() throws Exception {
        String formToValidate = loadResourceAsString(FULL_D8_FORM_JSON_PATH);

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(formToValidate))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("bulkScanCaseCreate")),
                        hasJsonPath("case_data.*", hasSize(13)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297")),
                            hasJsonPath("D8HelpWithFeesReferenceNumber", is("123456")),
                            hasJsonPath("D8PaymentMethod", is("card")),
                            hasJsonPath("D8PetitionerFirstName", is("Christopher")),
                            hasJsonPath("D8PetitionerLastName", is("O'John")),
                            hasJsonPath("D8PetitionerPhoneNumber", is("1111111111")),
                            hasJsonPath("D8PetitionerEmail", is("test.testerson@mailinator.com")),
                            hasJsonPath("D8LegalProcess", is("Divorce")),
                            hasJsonPath("D8ScreenHasMarriageCert", is("True")),
                            hasJsonPath("D8CertificateInEnglish", is("True")),
                            hasJsonPath("D8RespondentFirstName", is("Jane")),
                            hasJsonPath("D8RespondentLastName", is("Doe")),
                            hasJsonPath("D8RespondentPhoneNumber", is("22222222222"))
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingOnlyASubsetOfData() throws Exception {
        String formToValidate = loadResourceAsString(PARTIAL_D8_FORM_JSON_PATH);

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(formToValidate))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("bulkScanCaseCreate")),
                        hasJsonPath("case_data.*", hasSize(8)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297")),
                            hasJsonPath("D8HelpWithFeesReferenceNumber", is("123456")),
                            hasJsonPath("D8PaymentMethod", is("card")),
                            hasJsonPath("D8PetitionerFirstName", is("Christopher")),
                            hasJsonPath("D8PetitionerLastName", is("O'John")),
                            hasJsonPath("D8PetitionerPhoneNumber", is("1111111111")),
                            hasJsonPath("D8PetitionerEmail", is("test.testerson@mailinator.com")),
                            hasJsonPath("D8LegalProcess", is("Divorce"))
                        ))
                    ))
                )));
    }
}
