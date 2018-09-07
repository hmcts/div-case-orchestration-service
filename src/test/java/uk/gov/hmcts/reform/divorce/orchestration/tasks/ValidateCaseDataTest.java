package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class ValidateCaseDataTest {
    private ValidateCaseData validateCaseData;

    @Mock
    private CaseValidationClient caseValidationClient;
    private ValidationResponse validationResponse;
    private ValidationResponse invalidationResponse;
    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext context;

    @Before
    public void setUp() throws Exception {
        validateCaseData = new ValidateCaseData(caseValidationClient);
        validationResponse =
                ValidationResponse.builder()
                        .validationStatus("Pass")
                        .build();

        invalidationResponse =
                ValidationResponse.builder()
                        .validationStatus("Pass")
                        .errors(Arrays.asList("Invalid input"))
                        .build();

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(PIN,TEST_PIN );

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() throws TaskException {
        //given
        when(caseValidationClient.validate(any())).thenReturn(validationResponse);

        //when
        Map<String, Object> response = validateCaseData.execute(context, payload);

        //then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(PIN));
    }

    @Test
    public void executeShouldReturnUpdatedContextForInValidCase() throws TaskException {
        //given
        when(caseValidationClient.validate(any())).thenReturn(invalidationResponse);

        //when
        Map<String, Object> response = validateCaseData.execute(context, payload);

        //then
        assertNotNull(response);
        assertTrue(context.getStatus());
    }

    @After
    public void tearDown() throws Exception {
        validateCaseData = null;
    }
}