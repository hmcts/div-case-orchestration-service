package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;

import java.util.Date;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@RunWith(MockitoJUnitRunner.class)
public class RetryableBulkCaseWorkflowTest {

    private static final String TEST_CASE_ID_1 = "test_case_1";
    private static final String TEST_CASE_ID_2 = "test_case_2";
    private static final int MAX_RETRIES = 2;

    @Spy
    private RetryableBulkCaseWorkflow retryableBulkCaseWorkflow;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(retryableBulkCaseWorkflow, "maxRetries", MAX_RETRIES);
    }

    @Test(expected = BulkUpdateException.class)
    public void givenEmptyCases_thenThrowException() {
        retryableBulkCaseWorkflow.executeWithRetries(emptyMap(), TEST_CASE_ID, AUTH_TOKEN);
    }

    @Test
    public void whenExecuteWithRetries_thenAllCasesAreExecuted() throws WorkflowException {
        Map<String, Object> caseData1 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_1));
        Map<String, Object> caseData2 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_2));
        Map<String, Object> bulkCaseData = ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, asList(caseData1, caseData2));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, bulkCaseData);

        boolean executionSuccessful = retryableBulkCaseWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);

        assertThat(executionSuccessful, is(true));

        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_2, AUTH_TOKEN);
    }

    @Test
    public void givenServiceUnavailableException_whenExecuteWithRetries_thenExhaustAllTheRetries() throws WorkflowException {
        given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(new FeignException.ServiceUnavailable("Error", "Error".getBytes()));
    }

    @Test
    public void givenBadGatewayException_whenExecuteWithRetries_thenExhaustAllTheRetries() throws WorkflowException {
        given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(new FeignException.BadGateway("Error", "Error".getBytes()));
    }

    @Test
    public void givenGatewayTimeoutException_whenExecuteWithRetries_thenExhaustAllTheRetries() throws WorkflowException {
        given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(new FeignException.GatewayTimeout("Error", "Error".getBytes()));
    }

    @Test
    public void givenRetryableException_whenExecuteWithRetries_thenExhaustAllTheRetries() throws WorkflowException {
        RetryableException retryableException = new RetryableException(-1, "Error", Request.HttpMethod.GET, new Date());
        given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(retryableException);
    }

    @Test
    public void givenInternalServerErrorException_whenExecuteWithRetries_thenExhaustAllTheRetries() throws WorkflowException {
        given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(new FeignException.InternalServerError("Error", "Error".getBytes()));
    }

    public void given5xException_whenExecuteWithRetries_thenExhaustAllTheRetries(FeignException expectedException) throws WorkflowException {
        Map<String, Object> caseData1 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_1));
        Map<String, Object> caseData2 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_2));
        Map<String, Object> bulkCaseData = ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, asList(caseData1, caseData2));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, bulkCaseData);

        when(retryableBulkCaseWorkflow.run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN))
            .thenThrow(expectedException);

        boolean executionSuccessful = retryableBulkCaseWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);

        assertThat(executionSuccessful, is(false));

        verify(retryableBulkCaseWorkflow, times(MAX_RETRIES)).run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_2, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow,times(1)).notifyFailedCases(TEST_CASE_ID, asList(caseData1));
    }

    @Test
    public void given4xException_whenExecuteWithRetries_thenLogError() throws WorkflowException {
        Map<String, Object> caseData1 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_1));
        Map<String, Object> caseData2 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_2));
        Map<String, Object> bulkCaseData = ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, asList(caseData1, caseData2));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, bulkCaseData);

        when(retryableBulkCaseWorkflow.run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN))
            .thenThrow(new FeignException.NotAcceptable("Error", "Error".getBytes()));

        boolean executionSuccessful = retryableBulkCaseWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);

        assertThat(executionSuccessful, is(false));

        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_2, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow,times(1)).notifyFailedCases(TEST_CASE_ID, asList(caseData1));
    }

    @Test
    public void givenException_whenExecuteWithRetries_thenLogError() throws WorkflowException {
        Map<String, Object> caseData1 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_1));
        Map<String, Object> caseData2 = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID_2));
        Map<String, Object> bulkCaseData = ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, asList(caseData1, caseData2));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, bulkCaseData);

        when(retryableBulkCaseWorkflow.run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN))
            .thenThrow(new RuntimeException());
        when(retryableBulkCaseWorkflow.run(caseDetail, TEST_CASE_ID_2, AUTH_TOKEN))
            .thenThrow(new RuntimeException());
        boolean executionSuccessful = retryableBulkCaseWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);

        assertThat(executionSuccessful, is(false));

        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_1, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow, times(1)).run(caseDetail, TEST_CASE_ID_2, AUTH_TOKEN);
        verify(retryableBulkCaseWorkflow,times(1)).notifyFailedCases(TEST_CASE_ID, asList(caseData1, caseData2));
    }
}
