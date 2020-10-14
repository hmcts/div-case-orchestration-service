package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class TaskUtilsTest {

    @Test
    public void getMandatoryPropertyValueAsString() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", "testValue");

        String value = TaskUtils.getMandatoryPropertyValueAsString(caseDataPayload, "testKey");

        assertThat(value, equalTo("testValue"));
    }

    @Test
    public void getMandatoryPropertyValueAsLocalDate() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", "2019-05-13");

        LocalDate localDate = TaskUtils.getMandatoryPropertyValueAsLocalDateFromCCD(caseDataPayload, "testKey");

        assertThat(localDate.getDayOfMonth(), equalTo(13));
        assertThat(localDate.getMonth(), equalTo(Month.MAY));
        assertThat(localDate.getYear(), equalTo(2019));
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getMandatoryPropertyValueAsString(caseData, "testKey")
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"testKey\""));
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsNull() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", null);

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getMandatoryPropertyValueAsString(caseDataPayload, "testKey")
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"testKey\""));
    }

    @Test
    public void getMandatoryPropertyValueAsObject() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", Collections.emptyList());

        Object value = TaskUtils.getMandatoryPropertyValueAsObject(caseDataPayload, "testKey");

        assertThat(value, equalTo(Collections.emptyList()));
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryObjectFieldIsNull() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", null);

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getMandatoryPropertyValueAsObject(caseDataPayload, "testKey")
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"testKey\""));
    }

    @Test
    public void testCaseIdCanBeRetrieved() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "123");

        String caseId = TaskUtils.getCaseId(context);

        assertThat(caseId, equalTo("123"));
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsMissing() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getCaseId(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\""));
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsEmpty() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "");

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getCaseId(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\""));
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsNotString() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, 123);

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getCaseId(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\""));
    }

    @Test
    public void testAuthTokenCanBeRetrieved() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "123");

        String caseId = TaskUtils.getAuthToken(context);

        assertThat(caseId, equalTo("123"));
    }

    @Test
    public void testExceptionIsThrown_WhenAuthTokenIsMissing() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getAuthToken(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + AUTH_TOKEN_JSON_KEY + "\""));
    }

    @Test
    public void testExceptionIsThrown_WhenAuthTokenIsEmpty() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "");

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getAuthToken(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + AUTH_TOKEN_JSON_KEY + "\""));
    }

    @Test
    public void testExceptionIsThrown_WhenAuthTokenIsNotString() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, 123);

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getAuthToken(context)
        );
        assertThat(exception.getMessage(), is("Could not evaluate value of mandatory property \"" + AUTH_TOKEN_JSON_KEY + "\""));
    }

    @Test
    public void shouldThrowTaskExceptionWhenDateIsBadlyFormatted() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", "20190513");

        TaskException exception = assertThrows(TaskException.class, () ->
            TaskUtils.getMandatoryPropertyValueAsLocalDateFromCCD(caseDataPayload, "testKey")
        );
        assertThat(exception.getMessage(), is("Could not format date from \"testKey\" field."));
    }

}
