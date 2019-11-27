package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.update.in.BulkScanCaseUpdateRequest;

import java.util.HashMap;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class UpdateBulkScanITest {

    private static final String UPDATE_URL = "/update-case";

    private static final BulkScanCaseUpdateRequest VALID_BODY = new BulkScanCaseUpdateRequest(null, new HashMap<>());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @Before
    public void setup() {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonString(VALID_BODY))
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonString(VALID_BODY))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpoint() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonString(VALID_BODY))
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_update_details.*", hasSize(3)),
                    hasJsonPath("$.case_update_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("bulkScanCaseUpdate"))
                    ))
                )));
    }
}
