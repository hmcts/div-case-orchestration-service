package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULKCASE_CREATION_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_TITLE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_PARTIES_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COST_ORDER_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.FAMILY_MAN_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPROVAL_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.BulkCaseCreate.BULK_CASE_TITLE;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseCreateUTest {

    private static final String FAMILY_MAN_NUMBER = "fmNumber";
    private static final String CLAIM_COST_OPTION = "Yes";

    private static final String DN_APPROVAL_DATE = "1-1-2012";

    private static final String CURRENT_DATE = "13 May 2019";

    private static final String ERROR_CASE_ID = "errorCaseID";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private CcdUtil ccdUtilMock;

    @InjectMocks
    private BulkCaseCreate classToTest;


    @Before
    public void setUp() {
        setMinimumNumberOfCases(1);
    }

    @Test
    public void givenEmptyList_thenReturnEmptyResponse() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(SEARCH_RESULT_KEY, Collections.emptyList());
        Map<String, Object> response = classToTest.execute(context, null);

        assertTrue(response.isEmpty());
        verify(caseMaintenanceClient, never()).submitBulkCase(any(),any());
    }

    @Test
    public void givenSearchListWithLessThanMinimumNumber_thenDoNothing() {
        setMinimumNumberOfCases(10);

        TaskContext context = new DefaultTaskContext();
        SearchResult  searchResult = createSearchResult();
        context.setTransientObject(SEARCH_RESULT_KEY, Collections.singletonList(searchResult));
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        classToTest.execute(context, null);

        verify(caseMaintenanceClient, never()).submitBulkCase(any(),any());
    }

    @Test
    public void givenSearchList_thenProcessAll() {
        TaskContext context = new DefaultTaskContext();
        SearchResult  searchResult = createSearchResult();
        context.setTransientObject(SEARCH_RESULT_KEY, Collections.singletonList(searchResult));
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> cmsResponse = Collections.emptyMap();
        when(caseMaintenanceClient.submitBulkCase(bulkCaseFormat(), AUTH_TOKEN)).thenReturn(cmsResponse);

        Map<String, Object> response = classToTest.execute(context, null);

        Map<String, Object> expectedResponse = ImmutableMap.of(BULK_CASE_LIST_KEY, Collections.singletonList(cmsResponse));
        verify(caseMaintenanceClient, times(1)).submitBulkCase(bulkCaseFormat(), AUTH_TOKEN);
        assertThat(response, is(expectedResponse));
        assertNull(context.getTransientObject(BULKCASE_CREATION_ERROR));
    }

    @Test
    public void givenError_whenCreateBulkCase_thenReturnErrorOnContext() {
        TaskContext context = new DefaultTaskContext();
        SearchResult  searchResult = createSearchResult();
        SearchResult errorResult = SearchResult
            .builder()
            .cases(Collections.singletonList(CaseDetails.builder()
                .caseId(ERROR_CASE_ID)
                .caseData(Collections.emptyMap())
                .build()))
            .build();
        context.setTransientObject(SEARCH_RESULT_KEY, Arrays.asList(searchResult, errorResult));
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> cmsResponse = Collections.emptyMap();

        when(ccdUtilMock.getCurrentDateWithCustomerFacingFormat()).thenReturn(CURRENT_DATE);
        when(caseMaintenanceClient.submitBulkCase(bulkCaseFormat(), AUTH_TOKEN)).thenReturn(cmsResponse);
        when(caseMaintenanceClient.submitBulkCase(not(eq(bulkCaseFormat())), eq(AUTH_TOKEN)))
            .thenThrow(new FeignException.BadRequest("Request failed", "Request failed".getBytes()));

        Map<String, Object> response = classToTest.execute(context, null);

        Map<String, Object> expectedResponse = ImmutableMap.of(BULK_CASE_LIST_KEY, Collections.singletonList(cmsResponse));
        verify(caseMaintenanceClient, times(1)).submitBulkCase(bulkCaseFormat(), AUTH_TOKEN);
        assertThat(response, is(expectedResponse));
        assertThat(context.getTransientObject(BULKCASE_CREATION_ERROR), is(Collections.singletonList(ERROR_CASE_ID)));
    }

    private void setMinimumNumberOfCases(int minimunNoCases) {
        ReflectionTestUtils.setField(classToTest, "minimunCasesToProcess", minimunNoCases);
    }

    private Map<String, Object> bulkCaseFormat() {

        Map<String, Object> caseInBulk = new HashMap<>();
        String caseParties = String.format("%s %s vs %s %s", TEST_USER_FIRST_NAME, TEST_USER_LAST_NAME, TEST_USER_FIRST_NAME, TEST_USER_LAST_NAME);

        caseInBulk.put(CASE_REFERENCE_FIELD, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID));
        caseInBulk.put(CASE_PARTIES_FIELD, caseParties);
        caseInBulk.put(FAMILY_MAN_REFERENCE_FIELD, FAMILY_MAN_NUMBER);
        caseInBulk.put(COST_ORDER_FIELD, CLAIM_COST_OPTION);
        caseInBulk.put(BulkCaseConstants.DN_APPROVAL_DATE_FIELD, DN_APPROVAL_DATE);

        Map<String, Object> bulkCaseData = new HashMap<>();

        Map<String, String> caseReference = ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID);
        bulkCaseData.put(BULK_CASE_TITLE_KEY, String.format(BULK_CASE_TITLE, ccdUtilMock.getCurrentDateWithCustomerFacingFormat()));
        bulkCaseData.put(BULK_CASE_ACCEPTED_LIST_KEY, Collections.singletonList(ImmutableMap.of(VALUE_KEY, caseReference)));
        bulkCaseData.put(CASE_LIST_KEY, Collections.singletonList(ImmutableMap.of(VALUE_KEY, caseInBulk)));
        return bulkCaseData;
    }

    private SearchResult createSearchResult() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_USER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);

        caseData.put(D_8_CASE_REFERENCE, FAMILY_MAN_NUMBER);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, CLAIM_COST_OPTION);
        caseData.put(DN_APPROVAL_DATE_CCD_FIELD, DN_APPROVAL_DATE);

        return SearchResult.builder()
            .total(10)
            .cases(Collections.singletonList(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .caseData(caseData)
                    .build()
            ))
            .build();
    }
}
