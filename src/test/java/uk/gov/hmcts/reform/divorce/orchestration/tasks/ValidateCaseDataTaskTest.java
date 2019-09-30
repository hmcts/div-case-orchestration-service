package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
;import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Collections;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class ValidateCaseDataTaskTest {
    private ValidateCaseDataTask validateCaseDataTask;

    @Mock
    private ValidationService validationService;
    private ValidationResponse validationResponse;
    private ValidationResponse invalidationResponse;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        validateCaseDataTask = new ValidateCaseDataTask(validationService);
        validationResponse =
            ValidationResponse.builder()
                .validationStatus("Pass")
                .build();

        invalidationResponse =
            ValidationResponse.builder()
                .validationStatus("Pass")
                .errors(Collections.singletonList("Invalid input"))
                .build();

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(RESPONDENT_PIN, TEST_PIN);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() {
        //given
        when(validationService.validate(any())).thenReturn(validationResponse);

        //when
        Map<String, Object> response = validateCaseDataTask.execute(context, payload);

        //then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(RESPONDENT_PIN));
    }

    @Test
    public void executeShouldReturnUpdatedContextForInValidCase() {
        //given
        when(validationService.validate(any())).thenReturn(invalidationResponse);

        //when
        Map<String, Object> response = validateCaseDataTask.execute(context, payload);

        //then
        assertNotNull(response);
        assertTrue(context.hasTaskFailed());
    }

    @After
    public void tearDown() {
        validateCaseDataTask = null;
    }
}
