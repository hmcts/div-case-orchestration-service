package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidateCaseDataTest {

    private static final String FORM_ID = "case-progression";
    private static final String VALIDATION_ERROR =
            "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";

    @Mock
    CaseValidationClient caseValidationClient;

    @InjectMocks
    ValidateCaseData validateCaseData;

    private Map<String, Object> testData;
    private TaskContext context;
    private ValidationRequest validationRequest;
    private ValidationResponse validationResponse;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
        validationRequest = ValidationRequest.builder()
                .data(testData)
                .formId(FORM_ID)
                .build();
        validationResponse = ValidationResponse.builder().build();
    }

    @Test
    public void executeShouldCallValidationClientAndReturnTheCaseDataWithNoErrorsInContext() throws Exception {
        when(caseValidationClient.validate(validationRequest)).thenReturn(validationResponse);

        assertEquals(testData, validateCaseData.execute(context, testData));
        assertEquals(false, context.getStatus());
        assertEquals(null, context.getTransientObject(VALIDATION_ERROR));

        verify(caseValidationClient).validate(validationRequest);
    }

    @Test
    public void executeShouldCallValidationClientAndReturnTheCaseDataWithErrorsInContext() throws Exception {
        List<String> errors = new ArrayList<>();
        errors.add("An Error");
        validationResponse.setErrors(errors);

        when(caseValidationClient.validate(validationRequest)).thenReturn(validationResponse);

        assertEquals(testData, validateCaseData.execute(context, testData));
        assertEquals(true, context.getStatus());
        assertEquals(validationResponse,
                context.getTransientObject(VALIDATION_ERROR));

        verify(caseValidationClient).validate(validationRequest);
    }

    @Test(expected = TaskException.class)
    public void executeShouldThrowExceptionWhenValidationClientThrowsException() throws Exception {
        when(caseValidationClient.validate(validationRequest)).thenThrow(new RuntimeException());

        validateCaseData.execute(context, testData);

        verify(caseValidationClient).validate(validationRequest);
    }
}
