package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.elasticsearch.index.query.QueryBuilders;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_OVERDUE_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;
import static wiremock.org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;

@RunWith(MockitoJUnitRunner.class)
public class SearchCasesDAOverdueTaskTest {

    private static final String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    @Mock
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    private SearchCasesDAOverdueTask classUnderTest;

    private static final String TWO_MINUTES = "2m";
    private static final String TIME_SINCE_DA_WAS_PRONOUNCED = TWO_MINUTES;

    private DefaultTaskContext contextBeingModified;

    @Before
    public void setupTaskContextWithSearchSettings() {
        contextBeingModified = new DefaultTaskContext();
        contextBeingModified.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        contextBeingModified.setTransientObject(DA_OVERDUE_PERIOD_KEY, TIME_SINCE_DA_WAS_PRONOUNCED);

        classUnderTest = new SearchCasesDAOverdueTask(cmsElasticSearchSupport, TIME_SINCE_DA_WAS_PRONOUNCED);
    }

    @Test
    public void execute_pageSize10_totalResults0() throws TaskException {

        final List<String> expectedCaseIdsInTheContext = Collections.emptyList();
        when(cmsElasticSearchSupport.searchCMSCases(
            eq(AUTH_TOKEN),
            any(),
            any())
        ).thenReturn(Stream.empty());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, Collections.emptyMap());
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertEquals(actualResult, Collections.emptyMap());

        verify(cmsElasticSearchSupport).searchCMSCases(eq(AUTH_TOKEN),
            eq(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_DA)),
            eq(QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildDateForTodayMinusGivenPeriod(TIME_SINCE_DA_WAS_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults5() throws TaskException {

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5");

        when(cmsElasticSearchSupport.searchCMSCases(eq(AUTH_TOKEN), any(), any())).thenReturn(buildCases(5));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(cmsElasticSearchSupport).searchCMSCases(eq(AUTH_TOKEN), any());
    }

    @Test
    public void execute_pageSize10_totalResults10() throws TaskException {

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        when(cmsElasticSearchSupport.searchCMSCases(
            eq(AUTH_TOKEN),
            any(),
            any())
        ).thenReturn(buildCases(10));


        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(cmsElasticSearchSupport).searchCMSCases(eq(AUTH_TOKEN), any());
    }

    @Test
    public void execute_pageSize10_totalResults20() throws TaskException {

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20");

        when(cmsElasticSearchSupport.searchCMSCases(
            eq(AUTH_TOKEN),
            any(),
            any())
        ).thenReturn(buildCases(20));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(cmsElasticSearchSupport).searchCMSCases(eq(AUTH_TOKEN), any());
    }

    @Test
    public void execute_exceptionDuringSearch_searchStops() throws TaskException {
        when(cmsElasticSearchSupport.searchCMSCases(
            eq(AUTH_TOKEN),
            any(),
            any())
        ).thenThrow(new FeignException.BadRequest("Bad test request", "".getBytes()));

        try {
            classUnderTest.execute(contextBeingModified, Collections.emptyMap());
        } catch (FeignException fException) {
            assertThat(fException.status(), CoreMatchers.is(BAD_REQUEST_400));
            assertThat(fException.getMessage(), CoreMatchers.is("Bad test request"));
        }

        verify(cmsElasticSearchSupport).searchCMSCases(eq(AUTH_TOKEN), any());
    }

    private Stream<CaseDetails> buildCases(int caseCount) {
        final Stream.Builder<CaseDetails> streamBuilder = Stream.builder();

        for (int i = 0; i < caseCount; i++) {
            streamBuilder.add(CaseDetails.builder().caseId(String.valueOf(i + 1)).build());
        }
        return streamBuilder.build();
    }

}
