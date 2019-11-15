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

    private static final String EXCEPTION_RECORD_JSON_PATH = "jsonExamples/payloads/bulk/scan/basicForm.json";
    private static final String TRANSFORMATION_URL = "/transform-exception-record";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpoint() throws Exception {
        String formToValidate = loadResourceAsString(EXCEPTION_RECORD_JSON_PATH);

        mockMvc.perform(post(TRANSFORMATION_URL)
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
                        hasJsonPath("event_id", is("EVENT_ID")),
                        hasJsonPath("case_data.*", hasSize(2)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("D8FirstName", is("Christopher")),
                            hasJsonPath("D8LastName", is("O'John"))
                        ))
                    ))
                )));
    }
}
